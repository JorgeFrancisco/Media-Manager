package br.com.jorgemelo.nimbusfilemanager.shared.util;

/**
 * Progress arithmetic shared by every long-running operation the UI tracks
 * (dataset download and import, location rebuild, ...): percentage complete and
 * estimated time remaining from the average rate so far. Project standard:
 * visible progress always offers bar + percentage + time remaining whenever a
 * total is known; only the counter is shown when it is not.
 */
public final class ProgressMath {

	/** Below this elapsed time the average rate is too noisy for an estimate. */
	static final long MIN_ELAPSED_FOR_ETA_MILLIS = 2_000;

	private ProgressMath() {
	}

	/** 0-100 (capped), or -1 when the total is unknown. */
	public static int percent(long done, long total) {
		if (total <= 0) {
			return -1;
		}

		return Math.clamp(done * 100 / total, 0, 100);
	}

	/** Seconds remaining by average rate, or -1 when it cannot be estimated. */
	public static long etaSeconds(long elapsedMillis, long done, long total) {
		if (total <= 0 || done <= 0 || done > total || elapsedMillis < MIN_ELAPSED_FOR_ETA_MILLIS) {
			return -1;
		}

		return Math.max(0, elapsedMillis * (total - done) / done / 1000);
	}
}