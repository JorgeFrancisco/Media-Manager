package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums;

/**
 * What a visual fingerprint represents. A photo has a single
 * {@link #PHOTO_PHASH}; a video may later have several frame samples
 * (sample_index / position_ms on {@code media_fingerprint}), which is why the
 * results table is not one-row-per-photo.
 */
public enum FingerprintKind {

	PHOTO_PHASH
}