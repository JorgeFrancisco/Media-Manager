package br.com.jorgemelo.nimbusfilemanager.processing.application;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.processing.application.dto.CategorySnapshot;
import br.com.jorgemelo.nimbusfilemanager.processing.application.dto.Snapshot;
import br.com.jorgemelo.nimbusfilemanager.processing.domain.enums.ExternalToolCategory;

/**
 * Thread-safe accumulator that keeps the cost categories of parallel processing
 * strictly separated, so a report can tell where time actually goes:
 * <ul>
 * <li>time waiting in the executor queue (submitted, not yet started);</li>
 * <li>time waiting on an external-process gate (per
 * {@link ExternalToolCategory});</li>
 * <li>real execution time of ffmpeg/ffprobe (per category);</li>
 * <li>in-JVM extraction time (EXIF via metadata-extractor, MIME sniff, crypto
 * hash);</li>
 * <li>persistence time;</li>
 * <li>total wall time per task;</li>
 * <li>maximum observed concurrency;</li>
 * <li>counts of tasks executed, avoided by cache, cancelled and failed.</li>
 * </ul>
 *
 * <p>
 * <b>Accumulated vs wall-clock:</b> under parallelism the sum of individual
 * durations is expected to exceed the elapsed wall-clock time (several ffmpeg
 * run at once). The report must show both — accumulated ({@code *Nanos} sums)
 * and the real elapsed time ({@link #recordWallClock}) — and never treat the
 * raw sum of ffmpeg durations as a direct reduction of total time.
 */
@Component
public class ProcessingMetrics {

	private final LongAdder tasksExecuted = new LongAdder();
	private final LongAdder tasksCacheAvoided = new LongAdder();
	private final LongAdder tasksCancelled = new LongAdder();
	private final LongAdder tasksError = new LongAdder();

	private final LongAdder queueWaitNanos = new LongAdder();
	private final LongAdder taskTotalNanos = new LongAdder();
	private final LongAdder jvmExtractionNanos = new LongAdder();
	private final LongAdder persistenceNanos = new LongAdder();
	private final LongAdder wallClockNanos = new LongAdder();

	private final Map<ExternalToolCategory, LongAdder> gateWaitNanos = newCategoryMap();
	private final Map<ExternalToolCategory, LongAdder> externalExecNanos = newCategoryMap();
	private final Map<ExternalToolCategory, LongAdder> externalRuns = newCategoryMap();

	// Photo perceptual hashes split by whether the format is decodable in-JVM
	// (JPG/PNG
	// via ImageIO) or needs ffmpeg (HEIC/WEBP...). Used only to estimate how much
	// of the
	// ffmpeg cost a future in-JVM hash could remove.
	private final LongAdder photoHashJvmDecodable = new LongAdder();
	private final LongAdder photoHashFfmpegOnly = new LongAdder();
	private final LongAdder photoHashFailures = new LongAdder();

	private final AtomicInteger maxConcurrency = new AtomicInteger();

	void incExecuted() {
		tasksExecuted.increment();
	}

	public void incCacheAvoided() {
		tasksCacheAvoided.increment();
	}

	public void incCacheAvoided(long count) {
		tasksCacheAvoided.add(count);
	}

	void incCancelled() {
		tasksCancelled.increment();
	}

	void incError() {
		tasksError.increment();
	}

	void recordQueueWait(long nanos) {
		queueWaitNanos.add(nanos);
	}

	void recordTaskTotal(long nanos) {
		taskTotalNanos.add(nanos);
	}

	void recordJvmExtraction(long nanos) {
		jvmExtractionNanos.add(nanos);
	}

	void recordPersistence(long nanos) {
		persistenceNanos.add(nanos);
	}

	public void recordWallClock(long nanos) {
		wallClockNanos.add(nanos);
	}

	void recordGateWait(ExternalToolCategory category, long nanos) {
		gateWaitNanos.get(category).add(nanos);
	}

	void recordExternalExec(ExternalToolCategory category, long nanos) {
		externalExecNanos.get(category).add(nanos);
		externalRuns.get(category).increment();
	}

	/**
	 * Records that a photo perceptual hash was attempted, tagging whether its
	 * format could be decoded in-JVM (JPG/PNG) or requires ffmpeg (HEIC/WEBP...).
	 */
	void recordPhotoHashFormat(boolean jvmDecodable) {
		if (jvmDecodable) {
			photoHashJvmDecodable.increment();
		} else {
			photoHashFfmpegOnly.increment();
		}
	}

	/**
	 * Records a photo whose perceptual hash could not be computed (undecodable
	 * image).
	 */
	void recordPhotoHashFailure() {
		photoHashFailures.increment();
	}

	/** Records a newly observed concurrency level, keeping the running maximum. */
	void updateMaxConcurrency(int observed) {
		maxConcurrency.accumulateAndGet(observed, Math::max);
	}

	public void reset() {
		tasksExecuted.reset();
		tasksCacheAvoided.reset();
		tasksCancelled.reset();
		tasksError.reset();
		queueWaitNanos.reset();
		taskTotalNanos.reset();
		jvmExtractionNanos.reset();
		persistenceNanos.reset();
		wallClockNanos.reset();
		gateWaitNanos.values().forEach(LongAdder::reset);
		externalExecNanos.values().forEach(LongAdder::reset);
		externalRuns.values().forEach(LongAdder::reset);
		photoHashJvmDecodable.reset();
		photoHashFfmpegOnly.reset();
		photoHashFailures.reset();
		maxConcurrency.set(0);
	}

	public Snapshot snapshot() {
		Map<ExternalToolCategory, CategorySnapshot> categories = new EnumMap<>(ExternalToolCategory.class);

		for (ExternalToolCategory category : ExternalToolCategory.values()) {
			categories.put(category, new CategorySnapshot(externalRuns.get(category).sum(),
					gateWaitNanos.get(category).sum(), externalExecNanos.get(category).sum()));
		}

		return new Snapshot(tasksExecuted.sum(), tasksCacheAvoided.sum(), tasksCancelled.sum(), tasksError.sum(),
				queueWaitNanos.sum(), taskTotalNanos.sum(), jvmExtractionNanos.sum(), persistenceNanos.sum(),
				wallClockNanos.sum(), photoHashJvmDecodable.sum(), photoHashFfmpegOnly.sum(), photoHashFailures.sum(),
				maxConcurrency.get(), categories);
	}

	private static Map<ExternalToolCategory, LongAdder> newCategoryMap() {
		Map<ExternalToolCategory, LongAdder> map = new EnumMap<>(ExternalToolCategory.class);

		for (ExternalToolCategory category : ExternalToolCategory.values()) {
			map.put(category, new LongAdder());
		}

		return map;
	}
}