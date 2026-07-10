package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.rdcw;

/**
 * The recursive {@code ReadDirectoryChangesW} watch could not be opened on the
 * root (the directory handle could not be created). This is the only case where
 * the watcher falls all the way back to the per-directory {@code WatchService}
 * source - if even the single-handle recursive watch is unavailable.
 */
public class RdcwUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 8635217508028560412L;

	public RdcwUnavailableException(String message) {
		super(message);
	}
}