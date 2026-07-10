package br.com.jorgemelo.nimbusfilemanager.metadata.application;

/**
 * The catalog says the file is a photo, but its physical representation is a
 * known non-image container for which a perceptual hash cannot be computed.
 */
public class UnsupportedPhotoFingerprintException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public UnsupportedPhotoFingerprintException(String message) {
		super(message);
	}
}