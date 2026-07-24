package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums;

/**
 * What a visual fingerprint represents. A photo has a single
 * {@link #PHOTO_PHASH} sample; a video has several {@link #VIDEO_PHASH} frame
 * samples (sample_index / position_ms on {@code media_fingerprint}), which is
 * why the results table is not one-row-per-file.
 */
public enum FingerprintKind {

	PHOTO_PHASH,

	VIDEO_PHASH
}