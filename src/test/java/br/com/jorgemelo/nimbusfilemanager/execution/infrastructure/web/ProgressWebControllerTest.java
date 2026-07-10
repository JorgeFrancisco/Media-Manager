package br.com.jorgemelo.nimbusfilemanager.execution.infrastructure.web;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionCancellationService;

class ProgressWebControllerTest {

	@Test
	void progressShouldExposeExecutionIdAndKindToTemplate() {
		ExtendedModelMap model = new ExtendedModelMap();
		ExecutionCancellationService executionCancellationService = new ExecutionCancellationService();

		String view = new ProgressWebController(executionCancellationService).progress(5L, "organization-execute",
				model);

		Assertions.assertThat(view).isEqualTo("app/execution-progress");
		Assertions.assertThat(model).containsEntry("executionId", 5L).containsEntry("kind", "organization-execute");
	}

	@Test
	void cancelShouldRequestCancellationWhenExecutionIsRegistered() {
		ExecutionCancellationService executionCancellationService = new ExecutionCancellationService();
		ProgressWebController controller = new ProgressWebController(executionCancellationService);

		executionCancellationService.register(5L);

		Map<String, Boolean> notRunning = controller.cancel(99L);
		Map<String, Boolean> running = controller.cancel(5L);

		Assertions.assertThat(notRunning).isEqualTo(Map.of("requested", false));
		Assertions.assertThat(running).isEqualTo(Map.of("requested", true));
		Assertions.assertThat(executionCancellationService.isCancelled(5L)).isTrue();
	}
}