package br.com.jorgemelo.nimbusfilemanager.execution.infrastructure.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionRetentionService;

class SettingsExecutionRetentionWebControllerTest {

	private final ExecutionRetentionService retention = mock(ExecutionRetentionService.class);
	private final SettingsExecutionRetentionWebController controller = new SettingsExecutionRetentionWebController(
			retention);

	@Test
	void cleanupByAgeShouldDeleteFinishedOlderThanDays() {
		when(retention.deleteOlderThanDays(90)).thenReturn(3);

		RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

		Assertions.assertThat(controller.cleanupExecutions("age", 90, redirect)).isEqualTo("redirect:/app/settings");

		verify(retention).deleteOlderThanDays(90);

		Assertions.assertThat(redirect.getFlashAttributes().get("success").toString()).contains("3");
	}

	@Test
	void cleanupByKeepShouldKeepLatestExecutions() {
		when(retention.keepLatest(50)).thenReturn(1);

		RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

		Assertions.assertThat(controller.cleanupExecutions("keep", 50, redirect)).isEqualTo("redirect:/app/settings");

		verify(retention).keepLatest(50);

		Assertions.assertThat(redirect.getFlashAttributes().get("success").toString()).contains("1 execução removida");
	}

	@Test
	void cleanupShouldSurfaceValidationErrors() {
		doThrow(new IllegalArgumentException("O número de dias não pode ser negativo.")).when(retention)
				.deleteOlderThanDays(-1);

		RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

		controller.cleanupExecutions("age", -1, redirect);

		Assertions.assertThat(redirect.getFlashAttributes()).extractingByKey("error")
				.isEqualTo("O número de dias não pode ser negativo.");
	}
}