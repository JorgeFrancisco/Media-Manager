package br.com.jorgemelo.nimbusfilemanager.thumbnail.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PhotoThumbnailSource(UUID publicId, String currentPath, LocalDateTime modifiedAt, Integer rotation) {
}