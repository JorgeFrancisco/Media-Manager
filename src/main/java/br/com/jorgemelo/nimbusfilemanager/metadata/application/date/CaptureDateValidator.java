package br.com.jorgemelo.nimbusfilemanager.metadata.application.date;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class CaptureDateValidator {

	private static final int MIN_YEAR = 1995;
	private static final int MAX_FUTURE_YEARS = 1;

	private final Clock clock;

	public CaptureDateValidator(Clock clock) {
		this.clock = clock;
	}

	public LocalDateTime validate(LocalDateTime captureDate) {
		if (captureDate == null) {
			return null;
		}

		LocalDate today = LocalDate.now(clock);

		if (captureDate.toLocalDate().isAfter(today.plusYears(MAX_FUTURE_YEARS))) {
			return null;
		}

		if (captureDate.getYear() < MIN_YEAR) {
			return null;
		}

		return captureDate;
	}
}