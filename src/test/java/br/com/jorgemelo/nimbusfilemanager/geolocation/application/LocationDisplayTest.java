package br.com.jorgemelo.nimbusfilemanager.geolocation.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocationDisplayTest {

	@Test
	void fullLabelJoinsPresentPartsAndSkipsBlanks() {
		assertThat(LocationDisplay.fullLabel("Curitiba", "Paraná", "Brasil")).isEqualTo("Curitiba, Paraná, Brasil");
		// Each part is optional and blanks are skipped, but the remaining order is
		// preserved.
		assertThat(LocationDisplay.fullLabel("Curitiba", "  ", "Brasil")).isEqualTo("Curitiba, Brasil");
		assertThat(LocationDisplay.fullLabel(null, "Paraná", null)).isEqualTo("Paraná");
	}

	@Test
	void fullLabelStripsPartsAndReturnsNullWhenEmpty() {
		assertThat(LocationDisplay.fullLabel("  Curitiba  ", null, null)).isEqualTo("Curitiba");
		assertThat(LocationDisplay.fullLabel(null, "", "   ")).isNull();
	}

	@Test
	void shortLabelUsesCityAndStateAndDropsCountryWhenPresent() {
		// City/state present: the country is intentionally omitted from the compact
		// label.
		assertThat(LocationDisplay.shortLabel("Curitiba", "Paraná", "Brasil")).isEqualTo("Curitiba, Paraná");
	}

	@Test
	void shortLabelFallsBackToCountryOnlyWhenCityAndStateAreBlank() {
		assertThat(LocationDisplay.shortLabel(null, "  ", "Brasil")).isEqualTo("Brasil");
		assertThat(LocationDisplay.shortLabel(null, null, null)).isNull();
	}
}