package br.com.jorgemelo.nimbusfilemanager.statistics.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;

public record FolderStatisticsResponse(

		String folderPath,

		long files, long photos, long videos, long audios, long documents, long others,

		double percentage,

		SizeResponse totalSize) {
}