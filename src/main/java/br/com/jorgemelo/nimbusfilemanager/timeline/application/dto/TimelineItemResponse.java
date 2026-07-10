package br.com.jorgemelo.nimbusfilemanager.timeline.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.DateSource;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

public record TimelineItemResponse(UUID id, String fileName, FileType type, LocalDateTime captureDate,
		DateSource dateSource, Integer width, Integer height, Double durationSeconds, String thumbnailUrl,
		String location) {
}