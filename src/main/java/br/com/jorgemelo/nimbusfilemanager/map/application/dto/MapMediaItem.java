package br.com.jorgemelo.nimbusfilemanager.map.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

/**
 * Compact item shown in a pin's media panel: only what the list needs. The
 * thumbnail is fetched separately via {@code /api/media/{mediaId}/thumbnail}.
 */
public record MapMediaItem(UUID mediaId, FileType fileType, String fileName, LocalDateTime captureDate) {
}