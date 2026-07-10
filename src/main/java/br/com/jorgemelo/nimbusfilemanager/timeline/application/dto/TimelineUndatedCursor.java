package br.com.jorgemelo.nimbusfilemanager.timeline.application.dto;

import br.com.jorgemelo.nimbusfilemanager.timeline.domain.enums.TimelineMediaType;

public record TimelineUndatedCursor(long internalId, TimelineMediaType type) {
}