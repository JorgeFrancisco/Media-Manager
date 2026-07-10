package br.com.jorgemelo.nimbusfilemanager.duplicate.application;

/**
 * Reports deletion progress (files processed so far, and the total) to a
 * background runner.
 */
@FunctionalInterface
public interface DeletionProgressCallback {

	void update(int processed, int total);
}