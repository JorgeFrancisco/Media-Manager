package br.com.jorgemelo.nimbusfilemanager.timeline.application.dto;

import java.time.LocalDateTime;

import br.com.jorgemelo.nimbusfilemanager.timeline.domain.enums.TimelineMediaType;

public record TimelineCursor(LocalDateTime captureDate, long internalId, TimelineMediaType type) {
}