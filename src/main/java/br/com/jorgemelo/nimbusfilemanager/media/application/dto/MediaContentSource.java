package br.com.jorgemelo.nimbusfilemanager.media.application.dto;

import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

public record MediaContentSource(UUID publicId, String currentPath, String fileName, String mimeType,
		FileType fileType) {
}