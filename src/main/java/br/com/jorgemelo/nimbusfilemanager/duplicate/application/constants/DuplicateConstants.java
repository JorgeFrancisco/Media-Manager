package br.com.jorgemelo.nimbusfilemanager.duplicate.application.constants;


/**
 * Contract data constants for the duplicate domain: the similarity bounds shown
 * on the Fotos Semelhantes screen and the fingerprint algorithm identifier the
 * backlog job and its collaborators drain against.
 */
public final class DuplicateConstants {

	public static final int MIN_SIMILARITY_PERCENT = 70;
	public static final int MAX_SIMILARITY_PERCENT = 100;
	public static final String ALGORITHM = FingerprintAlgorithm.FFMPEG_LANCZOS_PHASH_256_V1;

	private DuplicateConstants() {
	}
}