package br.com.jorgemelo.nimbusfilemanager.metadata.application.date;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CaptureDateValidatorTest {

	@Test
	void validateShouldRejectNullOldAndFarFutureDates() {
		CaptureDateValidator validator = new CaptureDateValidator(Clock.systemDefaultZone());

		LocalDateTime valid = LocalDateTime.of(2024, Month.MAY, 9, 10, 30);

		Assertions.assertThat(validator.validate(null)).isNull();
		Assertions.assertThat(validator.validate(LocalDateTime.of(1994, Month.DECEMBER, 31, 23, 59))).isNull();
		Assertions.assertThat(validator.validate(LocalDateTime.now().plusYears(2))).isNull();
		Assertions.assertThat(validator.validate(valid)).isEqualTo(valid);
	}
}