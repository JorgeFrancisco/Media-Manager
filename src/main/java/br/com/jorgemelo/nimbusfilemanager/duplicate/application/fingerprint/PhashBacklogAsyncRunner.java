package br.com.jorgemelo.nimbusfilemanager.duplicate.application.fingerprint;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.duplicate.application.constants.DuplicateConstants;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.DrainResult;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.PhashBacklogStatus;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.FingerprintJobStatus;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model.FingerprintJobRun;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.FingerprintJobRunRepository;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.AsyncConfig;
import br.com.jorgemelo.nimbusfilemanager.shared.util.ProgressMath;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs the photo fingerprint backlog in the background. Lives in its own bean
 * so the {@code @Async} proxy is honored; callers do
 * {@code if (runner.start()) runner.run()} so the guard is evaluated
 * synchronously and the drain runs off-thread.
 *
 * <p>
 * {@link #start()} is idempotent and coordinated: it refuses to start while an
 * inventory is active, when there is nothing pending, or when a run is already
 * going (CAS). This is what makes startup, screen-open and retry all safe to
 * call without ever launching two jobs.
 */
@Slf4j
@Service
public class PhashBacklogAsyncRunner {

	private final PhashBacklogService backlogService;
	private final FingerprintJobRunRepository jobRunRepository;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicBoolean stopRequested = new AtomicBoolean(false);
	private final AtomicLong processed = new AtomicLong();
	private final AtomicLong failed = new AtomicLong();
	private final AtomicLong startedAtMillis = new AtomicLong();
	private final AtomicLong pendingAtStart = new AtomicLong();
	private final AtomicReference<Long> currentRunId = new AtomicReference<>();
	private final AtomicReference<String> lastError = new AtomicReference<>();
	private final Clock clock;

	public PhashBacklogAsyncRunner(PhashBacklogService backlogService, FingerprintJobRunRepository jobRunRepository,
			Clock clock) {
		this.backlogService = backlogService;
		this.jobRunRepository = jobRunRepository;
		this.clock = clock;
	}

	/**
	 * @return false (and starts nothing) when an inventory is active, nothing is
	 *         pending, or a run is already in progress. Idempotent.
	 */
	public synchronized boolean start() {
		if (backlogService.inventoryActive()) {
			return false;
		}

		PhashBacklogStatus status = backlogService.status();

		if (status.pending() == 0) {
			return false;
		}

		if (!running.compareAndSet(false, true)) {
			return false;
		}

		stopRequested.set(false);
		processed.set(0);
		failed.set(0);
		startedAtMillis.set(System.currentTimeMillis());
		pendingAtStart.set(status.pending());
		lastError.set(null);

		FingerprintJobRun run = jobRunRepository.save(FingerprintJobRun.builder().kind(PhashBacklogService.KIND)
				.algorithm(DuplicateConstants.ALGORITHM).status(FingerprintJobStatus.RUNNING)
				.startedAt(LocalDateTime.now(clock)).totalAtStart(status.total()).build());

		currentRunId.set(run.getId());

		return true;
	}

	/** Prepares a fingerprint-only rebuild without racing an active worker. */
	public synchronized boolean prepareRebuild() {
		if (running.get() || backlogService.inventoryActive()) {
			return false;
		}

		backlogService.rebuild();

		return start();
	}

	@Async(AsyncConfig.VISUAL_ANALYSIS_EXECUTOR)
	public void run() {
		FingerprintJobStatus finalStatus = FingerprintJobStatus.FINISHED;

		String message = null;

		try {
			DrainResult result = backlogService.drainPending(stopRequested::get,
					(done, failures) -> {
						processed.set(done);
						failed.set(failures);
					});

			if (stopRequested.get() || backlogService.inventoryActive()) {
				finalStatus = FingerprintJobStatus.CANCELLED;
			}

			message = "processed=" + result.processed() + ", failed=" + result.failed();
		} catch (RuntimeException e) {
			finalStatus = FingerprintJobStatus.FAILED;

			lastError.set(e.getMessage());

			message = e.getMessage();

			log.error("Photo fingerprint backlog failed", e);
		} finally {
			finalizeRun(finalStatus, message);

			running.set(false);
		}
	}

	private void finalizeRun(FingerprintJobStatus status, String message) {
		Long runId = currentRunId.getAndSet(null);

		if (runId == null) {
			return;
		}

		try {
			jobRunRepository.findById(runId).ifPresent(run -> {
				run.setStatus(status);
				run.setFinishedAt(LocalDateTime.now(clock));
				run.setProcessed(processed.get());
				run.setFailed(failed.get());
				run.setMessage(message);

				jobRunRepository.save(run);
			});
		} catch (RuntimeException e) {
			log.warn("Could not finalize fingerprint job run {}", runId, e);
		}
	}

	@PreDestroy
	public void stop() {
		stopRequested.set(true);
	}

	public boolean isRunning() {
		return running.get();
	}

	public long processed() {
		return processed.get();
	}

	public long failed() {
		return failed.get();
	}

	/**
	 * Estimated seconds remaining from the rate at which pending photos disappear.
	 */
	public long etaSeconds() {
		if (!running.get()) {
			return -1;
		}

		long initialPending = pendingAtStart.get();
		long currentPending = backlogService.status().pending();
		long completed = Math.max(0, initialPending - currentPending);

		return ProgressMath.etaSeconds(System.currentTimeMillis() - startedAtMillis.get(), completed, initialPending);
	}

	public String lastError() {
		return lastError.get();
	}

	public PhashBacklogStatus status() {
		return backlogService.status();
	}
}