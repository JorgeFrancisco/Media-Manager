package br.com.jorgemelo.nimbusfilemanager.metadata.application.dto;

import java.time.LocalDateTime;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.DateSource;

public record ResolvedMediaDate(LocalDateTime captureDate, DateSource dateSource) {

	public Integer year() {
		return captureDate == null ? null : captureDate.getYear();
	}

	public Integer month() {
		return captureDate == null ? null : captureDate.getMonthValue();
	}

	public Integer day() {
		return captureDate == null ? null : captureDate.getDayOfMonth();
	}

	public String yearMonth() {
		return year() != null && month() != null ? "%04d-%02d".formatted(year(), month()) : null;
	}
}