package br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection;

import java.time.LocalDateTime;

public record ErrorFileDetailsResponse(String path, String errorType, long occurrences, LocalDateTime firstOccurrence,
		LocalDateTime lastOccurrence) {
}