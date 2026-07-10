package br.com.jorgemelo.nimbusfilemanager.metadata.application.dto;

import java.time.LocalDateTime;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.DateSource;

public record ResolvedCaptureDate(LocalDateTime captureDate, DateSource dateSource) {
}