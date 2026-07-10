package br.com.jorgemelo.nimbusfilemanager.time.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.settings.application.AppSettingService;

class ConfigurableClockTest {

	@Test
	void getZoneShouldReflectTheConfiguredZoneOnEachCall() {
		AppSettingService settings = mock(AppSettingService.class);
		ConfigurableClock clock = new ConfigurableClock(settings);

		when(settings.zoneId()).thenReturn(ZoneId.of("America/Sao_Paulo"), ZoneId.of("Europe/Zurich"));

		Assertions.assertThat(clock.getZone()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
		Assertions.assertThat(clock.getZone()).isEqualTo(ZoneId.of("Europe/Zurich"));
	}

	@Test
	void instantShouldBeTheCurrentInstant() {
		AppSettingService settings = mock(AppSettingService.class);
		ConfigurableClock clock = new ConfigurableClock(settings);

		Assertions.assertThat(Duration.between(clock.instant(), Instant.now()).abs()).isLessThan(Duration.ofMinutes(1));
	}

	@Test
	void withZoneShouldReturnAClockFixedToThatZone() {
		AppSettingService settings = mock(AppSettingService.class);
		ConfigurableClock clock = new ConfigurableClock(settings);

		Assertions.assertThat(clock.withZone(ZoneId.of("UTC")).getZone()).isEqualTo(ZoneId.of("UTC"));
	}

	@Test
	void equalsAndHashCodeShouldTrackTheBackingSettingsService() {
		AppSettingService settings = mock(AppSettingService.class);
		ConfigurableClock clock = new ConfigurableClock(settings);
		ConfigurableClock same = new ConfigurableClock(settings);
		ConfigurableClock other = new ConfigurableClock(mock(AppSettingService.class));

		Assertions.assertThat(clock.equals(clock)).isTrue();
		Assertions.assertThat(clock.equals(same)).isTrue();
		Assertions.assertThat(clock).hasSameHashCodeAs(same);
		Assertions.assertThat(clock.equals(other)).isFalse();
		Assertions.assertThat(clock.equals(null)).isFalse();

		Object unrelatedObject = "not a clock";

		Assertions.assertThat(clock.equals(unrelatedObject)).isFalse();
	}
}