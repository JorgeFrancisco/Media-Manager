package br.com.jorgemelo.nimbusfilemanager.media.domain.repository.projection;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

/**
 * Cohesive parameter object carrying the optional media search filters bound
 * into {@code MediaSearchRepository.search}. Every field is nullable: a
 * {@code null} disables the corresponding predicate in the query.
 */
public record MediaSearchFilter(FileType fileType, String codec, String folder, String extension, Integer year,
		Integer month, Long minSizeBytes, Long maxSizeBytes) {
}