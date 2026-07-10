package br.com.jorgemelo.nimbusfilemanager.execution.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.util.UuidV7;

public record ExecutionStepResponse(UUID id, UUID executionId, String stepType, String path, String message,
		Integer filesFound, Integer filesAnalyzed, Integer cacheHits, Integer errors, LocalDateTime createdAt) {

	public ExecutionStepResponse(Long id, Long executionId, String stepType, String path, String message,
			Integer filesFound, Integer filesAnalyzed, Integer cacheHits, Integer errors, LocalDateTime createdAt) {
		this(UuidV7.fromLegacy(id), UuidV7.fromLegacy(executionId), stepType, path, message, filesFound, filesAnalyzed,
				cacheHits, errors, createdAt);
	}
}