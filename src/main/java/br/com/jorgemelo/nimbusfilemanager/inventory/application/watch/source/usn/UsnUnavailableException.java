package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.usn;

/**
 * The USN Change Journal cannot be used at all for a root (not NTFS, the volume
 * cannot be opened, missing privilege, ...). The provider catches this and
 * falls back to the portable {@code WatchService} source.
 */
public class UsnUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 1189916444089285451L;

	public UsnUnavailableException(String message) {
		super(message);
	}
}