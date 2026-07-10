package br.com.jorgemelo.nimbusfilemanager.statistics.domain.repository.projection;

public record StatisticsSummaryRawResponse(

		// Count
		long totalFiles, long photos, long videos, long audios, long documents, long others, long deleted,

		// Space
		long totalSizeBytes, long photoSizeBytes, long videoSizeBytes, long audioSizeBytes, long documentSizeBytes,
		long otherSizeBytes) {
}