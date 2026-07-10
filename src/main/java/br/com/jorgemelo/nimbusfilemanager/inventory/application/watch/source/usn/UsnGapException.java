package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.usn;

/**
 * Raised mid-read when the cursor can no longer be trusted while the source is
 * already running (the journal was deleted/recreated, or the requested USN aged
 * out). The change source catches it, forces a full reconcile and
 * resynchronizes the cursor to the journal's current end - it does not tear the
 * source down.
 */
public class UsnGapException extends RuntimeException {

	private static final long serialVersionUID = 453508281287634667L;

	public UsnGapException(String message) {
		super(message);
	}
}