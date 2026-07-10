package br.com.jorgemelo.nimbusfilemanager.statistics.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

public record ExtensionStatisticsResponse(String extension, FileType fileType, long files, double percentage,
		SizeResponse totalSize) {
}