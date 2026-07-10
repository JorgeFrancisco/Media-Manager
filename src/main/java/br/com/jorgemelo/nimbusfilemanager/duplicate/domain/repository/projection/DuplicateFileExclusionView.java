package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single file hidden from duplicate comparison, joined to its current path so
 * the Configurações management list can show a friendly location instead of a
 * bare public id.
 */
public record DuplicateFileExclusionView(Long id, UUID publicId, String currentPath, LocalDateTime createdAt) {
}