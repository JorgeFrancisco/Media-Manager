package br.com.jorgemelo.nimbusfilemanager.processing.domain.enums;

/**
 * Kinds of rate-limited external process the app currently spawns. Each
 * category has its own independent concurrency limit in
 * {@code ExternalToolGate}, because their costs differ sharply and must not
 * share a single limit.
 *
 * <p>
 * Only the categories actually used today are declared — no dead entries. A
 * future heavy category (for example a video H.265 transcode, whose per-file
 * cost dwarfs a quick frame/hash extraction) can be added here and given its
 * own limit without reworking the gate or the coordinator. EXIF is deliberately
 * absent: it runs in-JVM via the {@code metadata-extractor} library, not as an
 * external process, so it is bounded by the worker pool alone.
 */
public enum ExternalToolCategory {

	/**
	 * ffmpeg invoked to normalize one frame for a photo's 256-bit pHash and SSIM
	 * sample.
	 */
	FFMPEG_PHOTO_HASH,

	/** ffprobe invoked to read a video's stream/format metadata. */
	FFPROBE_VIDEO
}