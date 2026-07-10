package br.com.jorgemelo.nimbusfilemanager.statistics.domain.repository.projection;

public record FolderStatisticsRawResponse(

		String folderPath,

		long files, long photos, long videos, long audios, long documents, long others,

		double percentage,

		long totalSizeBytes) {
}