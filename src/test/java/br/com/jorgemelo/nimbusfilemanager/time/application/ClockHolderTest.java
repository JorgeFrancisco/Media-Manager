package br.com.jorgemelo.nimbusfilemanager.time.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.ClockHolder;

class ClockHolderTest {

	@AfterEach
	void resetHolder() {
		ClockHolder.reset();
	}

	@Test
	void defaultsToSaoPauloBeforeSpringWiresIt() {
		Assertions.assertThat(ClockHolder.clock().getZone()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
	}

	@Test
	void useReplacesTheClockAndResetRestoresTheDefault() {
		Clock fixed = Clock.fixed(Instant.parse("2024-01-02T03:04:05Z"), ZoneOffset.UTC);

		ClockHolder.configure(fixed);

		Assertions.assertThat(ClockHolder.clock()).isSameAs(fixed);

		ClockHolder.reset();

		Assertions.assertThat(ClockHolder.clock().getZone()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
	}

	@Test
	void useWithNullFallsBackToTheDefault() {
		ClockHolder.configure(null);

		Assertions.assertThat(ClockHolder.clock().getZone()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
	}

	@Test
	void initializerWiresTheGivenClockIntoTheHolder() {
		Clock fixed = Clock.fixed(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC);

		new ClockHolderInitializer(fixed);

		Assertions.assertThat(ClockHolder.clock()).isSameAs(fixed);
	}
}