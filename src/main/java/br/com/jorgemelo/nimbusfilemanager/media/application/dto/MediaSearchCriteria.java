package br.com.jorgemelo.nimbusfilemanager.media.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

public record MediaSearchCriteria(FileType fileType, String codec, String folder, String extension, Integer year,
		Integer month, Long minSizeBytes, Long maxSizeBytes) {
}