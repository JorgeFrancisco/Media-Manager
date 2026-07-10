package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection;

public interface DuplicateSummaryProjection {

	long getGroups();

	long getDuplicatedFiles();

	long getTotalSizeBytes();

	long getWastedSizeBytes();
}