package br.com.jorgemelo.nimbusfilemanager.duplicate.application.fingerprint;

/** Invoked after each persisted batch with the cumulative counts. */
@FunctionalInterface
public interface ProgressListener {

	void onProgress(long processed, long failed);
}