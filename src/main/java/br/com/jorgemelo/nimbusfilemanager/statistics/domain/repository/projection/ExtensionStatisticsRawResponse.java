package br.com.jorgemelo.nimbusfilemanager.statistics.domain.repository.projection;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

public record ExtensionStatisticsRawResponse(String extension, FileType fileType, long files, double percentage,
		long totalSizeBytes) {
}