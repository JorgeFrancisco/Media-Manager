package br.com.jorgemelo.nimbusfilemanager.duplicate.application.fingerprint;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.jorgemelo.nimbusfilemanager.duplicate.application.constants.DuplicateConstants;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.BatchCounts;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.DrainResult;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.PhashBacklogStatus;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.FingerprintKind;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model.FingerprintFailure;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model.MediaFingerprint;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.FingerprintFailureRepository;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.MediaFingerprintRepository;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection.PendingPhoto;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection.PhotoFingerprintFailureResponse;
import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionQueryService;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.PhotoPerceptualHashService;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.UnsupportedPhotoFingerprintException;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.PhotoPerceptualFingerprint;
import br.com.jorgemelo.nimbusfilemanager.processing.application.ProcessingCoordinator;
import br.com.jorgemelo.nimbusfilemanager.processing.application.dto.Outcome;

/**
 * Computes the photo perceptual-hash backlog OUTSIDE the inventory: it drains
 * the derived pending queue in parallel (reusing {@link ProcessingCoordinator}
 * and, through {@link PhotoPerceptualHashService}, the ffmpeg
 * {@code ExternalToolGate}), persisting each batch in its own short
 * transaction. Because "done"/"failed" are the
 * {@code media_fingerprint}/{@code fingerprint_failure} rows themselves, a
 * crash only loses the in-flight batch and the next run re-derives the rest.
 *
 * <p>
 * It never runs while an inventory is active: the drain checks between batches
 * and pauses (the remaining work is simply still pending). Failures are retried
 * up to {@link #MAX_ATTEMPTS} (attempts incremented each time) and then
 * excluded from the pending queue, so a permanently undecodable photo never
 * blocks the screen forever.
 */
@Service
public class PhashBacklogService {

	static final FingerprintKind KIND = FingerprintKind.PHOTO_PHASH;
	static final int BATCH_SIZE = 200;
	static final int MAX_ATTEMPTS = 3;
	static final String UNSUPPORTED_PREFIX = "[unsupported] ";
	private static final int MAX_ERROR_LENGTH = 500;

	private final MediaFingerprintRepository mediaFingerprintRepository;
	private final FingerprintFailureRepository fingerprintFailureRepository;
	private final PhotoPerceptualHashService photoPerceptualHashService;
	private final ProcessingCoordinator processingCoordinator;
	private final ExecutionQueryService executionQueryService;
	private final TransactionTemplate writeTransaction;
	private final Clock clock;

	public PhashBacklogService(MediaFingerprintRepository mediaFingerprintRepository,
			FingerprintFailureRepository fingerprintFailureRepository,
			PhotoPerceptualHashService photoPerceptualHashService, ProcessingCoordinator processingCoordinator,
			ExecutionQueryService executionQueryService, PlatformTransactionManager transactionManager, Clock clock) {
		this.mediaFingerprintRepository = mediaFingerprintRepository;
		this.fingerprintFailureRepository = fingerprintFailureRepository;
		this.photoPerceptualHashService = photoPerceptualHashService;
		this.processingCoordinator = processingCoordinator;
		this.executionQueryService = executionQueryService;
		this.writeTransaction = new TransactionTemplate(transactionManager);
		this.writeTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		this.clock = clock;
	}

	/**
	 * True while an inventory execution is active - the fingerprint job yields to
	 * it.
	 */
	public boolean inventoryActive() {
		return executionQueryService.active().map(execution -> "INVENTORY".equals(execution.executionType()))
				.orElse(false);
	}

	public PhashBacklogStatus status() {
		long done = mediaFingerprintRepository.countByKindAndAlgorithm(KIND, DuplicateConstants.ALGORITHM);

		long failed = fingerprintFailureRepository.countExhaustedFailures(KIND, DuplicateConstants.ALGORITHM, MAX_ATTEMPTS,
				UNSUPPORTED_PREFIX);

		long pending = mediaFingerprintRepository.countPendingPhotos(KIND, DuplicateConstants.ALGORITHM, MAX_ATTEMPTS);

		return new PhashBacklogStatus(pending, done, failed);
	}

	/** Exhausted items displayed on demand by the failure-details modal. */
	public List<PhotoFingerprintFailureResponse> failures() {
		return fingerprintFailureRepository.findExhaustedWithPath(KIND, DuplicateConstants.ALGORITHM, MAX_ATTEMPTS, UNSUPPORTED_PREFIX);
	}

	/** Manual retry: exhausted failures return to the pending queue. */
	public long resetFailures() {
		return fingerprintFailureRepository.deleteRetryableByKindAndAlgorithm(KIND, DuplicateConstants.ALGORITHM, UNSUPPORTED_PREFIX);
	}

	/**
	 * Clears only derived pHash/SSIM data; inventory, metadata and SHA-256 are
	 * untouched.
	 */
	public long rebuild() {
		Long removed = writeTransaction.execute(_ -> {
			fingerprintFailureRepository.deleteByKindAndAlgorithm(KIND, DuplicateConstants.ALGORITHM);

			return mediaFingerprintRepository.deleteByKindAndAlgorithm(KIND, DuplicateConstants.ALGORITHM);
		});

		return removed == null ? 0 : removed;
	}

	/**
	 * Drains the pending queue until empty, cancelled, or an inventory takes
	 * priority. The heavy hashing runs off-transaction on the coordinator; only the
	 * per-batch persistence is transactional.
	 */
	DrainResult drainPending(BooleanSupplier stop, ProgressListener progress) {
		long processed = 0;

		long failed = 0;

		while (!stop.getAsBoolean() && !inventoryActive()) {
			List<PendingPhoto> batch = deduplicate(mediaFingerprintRepository.findPendingPhotos(KIND, DuplicateConstants.ALGORITHM,
					MAX_ATTEMPTS, PageRequest.of(0, BATCH_SIZE)));

			if (batch.isEmpty()) {
				break;
			}

			List<Outcome<PendingPhoto, PhotoPerceptualFingerprint>> outcomes = processingCoordinator.process(batch,
					stop, this::computeHash);
			BatchCounts counts = Objects.requireNonNull(writeTransaction.execute(_ -> persistBatch(outcomes)));

			processed += counts.done();

			failed += counts.failed();

			progress.onProgress(processed, failed);
		}

		return new DrainResult(processed, failed);
	}

	private PhotoPerceptualFingerprint computeHash(PendingPhoto photo) {
		return photoPerceptualHashService.compute(Path.of(photo.path()));
	}

	private BatchCounts persistBatch(List<Outcome<PendingPhoto, PhotoPerceptualFingerprint>> outcomes) {
		long done = 0;

		long failed = 0;

		for (Outcome<PendingPhoto, PhotoPerceptualFingerprint> outcome : outcomes) {
			PendingPhoto photo = outcome.item();

			if (outcome.executed()) {
				storeFingerprint(photo, outcome.value());

				done++;
			} else if (outcome.failed()) {
				recordFailure(photo, outcome.error());

				failed++;
			}
			// CANCELLED: left pending, picked up by the next run.
		}

		return new BatchCounts(done, failed);
	}

	private void storeFingerprint(PendingPhoto photo, PhotoPerceptualFingerprint fingerprint) {
		if (!mediaFingerprintRepository.existsByCatalogFileIdAndKindAndAlgorithmAndSampleIndex(photo.catalogFileId(), KIND,
				DuplicateConstants.ALGORITHM, 0)) {
			mediaFingerprintRepository.save(MediaFingerprint.builder().catalogFileId(photo.catalogFileId()).kind(KIND)
					.algorithm(DuplicateConstants.ALGORITHM).sampleIndex(0).hashBytes(fingerprint.hash())
					.sampleBytes(fingerprint.luminance()).computedAt(LocalDateTime.now(clock)).build());
		}

		// A prior failed attempt that later succeeds must not linger as a failure.
		fingerprintFailureRepository.deleteByCatalogFileIdAndKindAndAlgorithm(photo.catalogFileId(), KIND, DuplicateConstants.ALGORITHM);
	}

	private void recordFailure(PendingPhoto photo, Exception error) {
		FingerprintFailure failure = fingerprintFailureRepository
				.findByCatalogFileIdAndKindAndAlgorithm(photo.catalogFileId(), KIND, DuplicateConstants.ALGORITHM)
				.orElseGet(() -> FingerprintFailure.builder().catalogFileId(photo.catalogFileId()).kind(KIND)
						.algorithm(DuplicateConstants.ALGORITHM).attempts(0).build());

		boolean unsupported = error instanceof UnsupportedPhotoFingerprintException;

		failure.setAttempts(unsupported ? MAX_ATTEMPTS : failure.getAttempts() + 1);
		failure.setLastError((unsupported ? UNSUPPORTED_PREFIX : "") + truncate(error));
		failure.setLastAttemptAt(LocalDateTime.now(clock));

		fingerprintFailureRepository.save(failure);
	}

	private String truncate(Exception error) {
		String message = error == null ? "unknown error" : String.valueOf(error.getMessage());

		return message.length() <= MAX_ERROR_LENGTH ? message : message.substring(0, MAX_ERROR_LENGTH);
	}

	private List<PendingPhoto> deduplicate(List<PendingPhoto> rows) {
		Map<Long, PendingPhoto> byId = new LinkedHashMap<>();

		for (PendingPhoto row : rows) {
			byId.putIfAbsent(row.catalogFileId(), row);
		}

		return new ArrayList<>(byId.values());
	}
}