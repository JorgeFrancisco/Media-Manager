package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import java.util.List;

import br.com.jorgemelo.nimbusfilemanager.media.domain.enums.MediaTypeFilter;

public record DuplicatesViewRequest(String tab, Integer page, Integer minSimilarity, String view, Integer size,
		List<MediaTypeFilter> types) {
}