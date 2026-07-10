package br.com.jorgemelo.nimbusfilemanager.duplicate.application;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.AsyncConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Computes the "Fotos semelhantes" grouping (clustering + SSIM) in the
 * background so the Duplicados screen never blocks on it. Lives in its own bean
 * so the {@code @Async} proxy is honored; callers do
 * {@code if (runner.start(minSim)) runner.run(minSim)} - the guard is evaluated
 * synchronously and the heavy work runs off-thread. Only one run happens at a
 * time; a request for another threshold while a run is in progress is simply
 * retried on the next screen refresh once the current run frees the lock.
 */
@Slf4j
@Service
public class PhotoSimilarityAsyncRunner {

	private final PhotoSimilarityService photoSimilarityService;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicInteger processed = new AtomicInteger();
	private final AtomicInteger total = new AtomicInteger();

	public PhotoSimilarityAsyncRunner(PhotoSimilarityService photoSimilarityService) {
		this.photoSimilarityService = photoSimilarityService;
	}

	/**
	 * Claims a background grouping for the given threshold, unless the result is
	 * already cached or a run is already in progress. Returns true only when a run
	 * was claimed; the caller then invokes {@link #run(int)} (the off-thread
	 * method).
	 */
	public synchronized boolean start(int minSimilarityPercent) {
		if (photoSimilarityService.isCached(minSimilarityPercent)) {
			return false;
		}

		if (!running.compareAndSet(false, true)) {
			return false;
		}

		processed.set(0);

		total.set(0);

		return true;
	}

	@Async(AsyncConfig.VISUAL_ANALYSIS_EXECUTOR)
	public void run(int minSimilarityPercent) {
		try {
			photoSimilarityService.computeAndCache(minSimilarityPercent, (done, count) -> {
				processed.set(done);
				total.set(count);
			});
		} catch (RuntimeException e) {
			log.error("Photo similarity grouping failed for minSimilarity={}", minSimilarityPercent, e);
		} finally {
			running.set(false);
		}
	}

	public boolean isRunning() {
		return running.get();
	}

	public int processed() {
		return processed.get();
	}

	public int total() {
		return total.get();
	}

	/**
	 * Percent complete (0-100) of the current grouping, or 0 while the candidate
	 * count is unknown.
	 */
	public int percent() {
		int candidates = total.get();

		return candidates <= 0 ? 0 : Math.min(100, (int) Math.round(processed.get() * 100.0 / candidates));
	}
}