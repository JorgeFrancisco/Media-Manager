package br.com.jorgemelo.nimbusfilemanager.organization.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.FileHashService;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.MoveBaseline;

/**
 * Strong post-move integrity verification.
 *
 * <p>
 * The baseline (size + SHA-256) is captured from the <em>source</em> file
 * itself right before the move, so it is the ground truth of exactly what is
 * being relocated - immune to a stale catalog hash. After the move the target
 * is checked against that baseline: the source must be gone, the target must
 * exist, its size must match, and its SHA-256 must match. Any mismatch is a
 * {@link MoveIntegrityException}, which the executor turns into an
 * {@code INTEGRITY_CHECK_FAILED} movement and a physical rollback.
 *
 * <p>
 * Reading every byte twice (source before, target after) is deliberate: the
 * project prioritizes maximum safety over I/O for large runs. The check is a
 * dedicated component so a lighter mode (size-only, or same-volume skip) can be
 * introduced later without touching the executor.
 */
@Component
public class OrganizationMoveVerifier {

	private final FileHashService fileHashService;

	@Autowired
	public OrganizationMoveVerifier(FileHashService fileHashService) {
		this.fileHashService = fileHashService;
	}

	/**
	 * Reads the source's size and SHA-256 before it is moved. Must be called while
	 * the source still exists on disk.
	 */
	public MoveBaseline capture(Path source) throws IOException {
		long size = Files.size(source);

		String sha256 = fileHashService.sha256(source);

		return new MoveBaseline(size, sha256);
	}

	/**
	 * Verifies the target against the baseline captured before the move. Throws
	 * {@link MoveIntegrityException} on the first discrepancy.
	 */
	public void verify(Path source, Path target, MoveBaseline baseline) {
		if (Files.exists(source)) {
			throw new MoveIntegrityException("source still exists after move: " + source);
		}

		if (!Files.exists(target)) {
			throw new MoveIntegrityException("target does not exist after move: " + target);
		}

		long targetSize = readSize(target);

		if (targetSize != baseline.sizeBytes()) {
			throw new MoveIntegrityException("target size " + targetSize + " differs from source size "
					+ baseline.sizeBytes() + " for " + target);
		}

		String targetSha256 = fileHashService.sha256(target);

		if (!targetSha256.equalsIgnoreCase(baseline.sha256())) {
			throw new MoveIntegrityException(
					"target SHA-256 does not match source SHA-256 for " + target + " (data corruption on move).");
		}
	}

	private long readSize(Path target) {
		try {
			return Files.size(target);
		} catch (IOException e) {
			throw new MoveIntegrityException(
					"could not read target size after move: " + target + ": " + e.getMessage());
		}
	}
}