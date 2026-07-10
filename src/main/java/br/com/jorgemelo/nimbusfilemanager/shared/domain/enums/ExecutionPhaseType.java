package br.com.jorgemelo.nimbusfilemanager.shared.domain.enums;

/**
 * Phases whose wall time is measured and persisted per execution for
 * performance telemetry. Inventory uses {@link #SCAN_COUNT},
 * {@link #CACHE_CHECK}, {@link #EXTRACTION} and {@link #PERSISTENCE};
 * Organization uses {@link #PLAN}, {@link #MOVEMENT} and {@link #RECONCILE}.
 */
public enum ExecutionPhaseType {

	/** Pre-scan that counts files to compute the progress denominator. */
	SCAN_COUNT,

	/** Short read transaction that identifies already-catalogued files. */
	CACHE_CHECK,

	/** Parallel metadata extraction (MIME, EXIF, hash, ffmpeg, ffprobe). */
	EXTRACTION,

	/** Short write transaction that persists the batch. */
	PERSISTENCE,

	/** Organization: building the move plan. */
	PLAN,

	/** Organization: applying file movements. */
	MOVEMENT,

	/** Organization: reconciling catalog against disk. */
	RECONCILE
}