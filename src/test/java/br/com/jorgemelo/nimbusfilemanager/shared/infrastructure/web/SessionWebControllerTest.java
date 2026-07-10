package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SessionWebControllerTest {

	@Test
	void keepAliveShouldReturnOk() {
		Map<String, Boolean> response = new SessionWebController().keepAlive();

		Assertions.assertThat(response).isEqualTo(Map.of("ok", true));
	}
}