package br.com.jorgemelo.nimbusfilemanager.duplicate.application;

/**
 * Reports grouping progress (candidates processed so far, and the total) to a
 * background runner.
 */
@FunctionalInterface
public interface SimilarityProgressCallback {

	void update(int processed, int total);
}