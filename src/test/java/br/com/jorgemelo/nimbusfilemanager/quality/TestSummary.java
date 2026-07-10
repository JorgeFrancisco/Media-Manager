package br.com.jorgemelo.nimbusfilemanager.quality;

/**
 * Mutable accumulator for the Surefire test totals aggregated by
 * {@link QualitySummary}.
 */
final class TestSummary {

	long tests;
	long failures;
	long errors;
	long skipped;
}