package br.com.jorgemelo.nimbusfilemanager.metadata.application.dto;

import java.time.LocalDateTime;

/**
 * Result of a single combined filesystem attribute read, carrying both the
 * created and modified dates so callers avoid issuing two separate {@code stat}
 * calls per file.
 */
public record FileSystemDates(LocalDateTime createdAt, LocalDateTime modifiedAt) {
}