package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class WellKnownWebControllerTest {

	@Test
	void wellKnownShouldIgnoreChromeDevtoolsProbe() {
		var response = new WellKnownWebController().chromeDevtools();

		Assertions.assertThat(response.getStatusCode().value()).isEqualTo(204);
		Assertions.assertThat(response.getBody()).isNull();
	}
}