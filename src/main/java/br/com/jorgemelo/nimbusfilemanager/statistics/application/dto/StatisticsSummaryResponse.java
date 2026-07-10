package br.com.jorgemelo.nimbusfilemanager.statistics.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;

public record StatisticsSummaryResponse(

		// Count
		long totalFiles, long photos, long videos, long audios, long documents, long others, long deleted,

		// Space
		SizeResponse totalSize, SizeResponse photoSize, SizeResponse videoSize, SizeResponse audioSize,
		SizeResponse documentSize, SizeResponse otherSize) {
}