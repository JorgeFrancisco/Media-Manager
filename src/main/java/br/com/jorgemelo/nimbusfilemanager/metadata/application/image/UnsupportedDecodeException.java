package br.com.jorgemelo.nimbusfilemanager.metadata.application.image;

/**
 * A photo whose format <b>no</b> registered {@link javax.imageio.ImageReader}
 * can decode (for example HEIC/HEIF with no plugin on the classpath). This is a
 * normal, expected outcome - the item is recorded as {@code UNSUPPORTED}, never
 * retried as a failure - and is deliberately <b>distinct</b> from a
 * corrupt/truncated file of a supported format, which surfaces as an
 * {@link java.io.IOException}.
 */
public class UnsupportedDecodeException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnsupportedDecodeException(String message) {
		super(message);
	}

	public UnsupportedDecodeException(String message, Throwable cause) {
		super(message, cause);
	}
}