package br.com.jorgemelo.nimbusfilemanager.metadata.application.constants;

/**
 * Contract data constants for the metadata domain: the fixed dimensions of the
 * normalized luminance sample and the packed 256-bit pHash produced by
 * {@link PhotoPerceptualHashService} and consumed by the similarity comparison.
 */
public final class MetadataConstants {

	public static final int SAMPLE_SIDE = 32;
	public static final int SAMPLE_BYTES = SAMPLE_SIDE * SAMPLE_SIDE;
	public static final int HASH_BYTES = 256 / Byte.SIZE;

	private MetadataConstants() {
	}
}