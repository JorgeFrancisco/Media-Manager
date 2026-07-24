package br.com.jorgemelo.nimbusfilemanager.duplicate.application.constants;

/**
 * Algorithm identifiers for visual fingerprints. The string fully identifies
 * hash <b>compatibility</b>: decoder + scaler + grayscale + hash + version.
 * Fingerprints with different identifiers are never compared to each other, so
 * changing any stage of the pipeline (e.g. moving photo decoding from
 * ffmpeg/lanczos to an in-JVM scaler) MUST be a new identifier - never a silent
 * change of an existing one.
 */
public final class FingerprintAlgorithm {

	/** Current photo fingerprint: 32x32 luminance sample and 256-bit DCT pHash. */
	public static final String FFMPEG_LANCZOS_PHASH_256_V1 = "FFMPEG_LANCZOS_PHASH_256_V1";

	/**
	 * Current video fingerprint: several frames sampled at deterministic relative
	 * positions, each normalized to the same 32x32 luminance sample and 256-bit DCT
	 * pHash as the photo algorithm. One {@code media_fingerprint} row per frame
	 * (sample_index = relative position, position_ms = sampled timestamp).
	 */
	public static final String FFMPEG_LANCZOS_PHASH_256_FRAMES_V1 = "FFMPEG_LANCZOS_PHASH_256_FRAMES_V1";

	private FingerprintAlgorithm() {
	}
}