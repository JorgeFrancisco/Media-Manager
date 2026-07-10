package br.com.jorgemelo.nimbusfilemanager.execution.infrastructure.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionQueryService;
import br.com.jorgemelo.nimbusfilemanager.execution.application.dto.ExecutionResponse;
import br.com.jorgemelo.nimbusfilemanager.organization.application.OrganizationService;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationUndoResponse;

/**
 * The public UUID-based endpoints of the execution controller: detail
 * rendering, redirect to the live progress screen while a run is still in
 * progress (with the right {@code kind}), and undo.
 */
class ExecutionWebControllerTest {

	private static final LocalDateTime NOW = LocalDateTime.parse("2026-07-12T12:00:00");
	private static final UUID ID = UUID.fromString("01890000-0000-7000-8000-000000000001");

	private final ExecutionQueryService executionQueryService = mock(ExecutionQueryService.class);
	private final OrganizationService organizationService = mock(OrganizationService.class);
	private final ExecutionWebController controller = new ExecutionWebController(executionQueryService,
			organizationService);

	@Test
	void executionRendersDetailWhenFinished() {
		when(executionQueryService.get(ID)).thenReturn(response("INVENTORY", "FINISHED", null));
		when(executionQueryService.steps(ID)).thenReturn(List.of());
		when(executionQueryService.errors(ID)).thenReturn(List.of());
		when(executionQueryService.movements(ID)).thenReturn(List.of());

		ExtendedModelMap model = new ExtendedModelMap();

		String view = controller.execution(ID, model);

		Assertions.assertThat(view).isEqualTo("app/execution-detail");
		Assertions.assertThat(model.get("execution")).isNotNull();
		Assertions.assertThat(model).containsKeys("steps", "errors", "movements");
	}

	@Test
	void executionRedirectsToInventoryProgressWhileRunning() {
		when(executionQueryService.get(ID)).thenReturn(response("INVENTORY", "SCANNING_FILES", null));

		Assertions.assertThat(controller.execution(ID, new ExtendedModelMap()))
				.isEqualTo("redirect:/app/progress/" + ID + "?kind=inventory");
	}

	@Test
	void executionRedirectsWithOrganizationExecuteKind() {
		when(executionQueryService.get(ID)).thenReturn(response("ORGANIZATION", "PROCESSING_FILES", true));

		Assertions.assertThat(controller.execution(ID, new ExtendedModelMap()))
				.isEqualTo("redirect:/app/progress/" + ID + "?kind=organization-execute");
	}

	@Test
	void executionRedirectsWithOrganizationPreviewKind() {
		when(executionQueryService.get(ID)).thenReturn(response("ORGANIZATION", "STARTED", false));

		Assertions.assertThat(controller.execution(ID, new ExtendedModelMap()))
				.isEqualTo("redirect:/app/progress/" + ID + "?kind=organization-preview");
	}

	@Test
	void undoRendersDetailUsingUndoPublic() {
		when(executionQueryService.get(ID)).thenReturn(response("ORGANIZATION", "FINISHED", true));
		when(organizationService.undoPublic(ID)).thenReturn(mock(OrganizationUndoResponse.class));
		when(executionQueryService.steps(ID)).thenReturn(List.of());
		when(executionQueryService.errors(ID)).thenReturn(List.of());
		when(executionQueryService.movements(ID)).thenReturn(List.of());

		ExtendedModelMap model = new ExtendedModelMap();

		String view = controller.undo(ID, model);

		Assertions.assertThat(view).isEqualTo("app/execution-detail");
		Assertions.assertThat(model.get("undo")).isNotNull();
	}

	@Test
	void executionShouldRenderDetailWhenFinished() {
		ExtendedModelMap model = new ExtendedModelMap();
		ExecutionResponse execution = execution();

		when(executionQueryService.get(1L)).thenReturn(execution);
		when(executionQueryService.steps(1L)).thenReturn(List.of());
		when(executionQueryService.errors(1L)).thenReturn(List.of());
		when(executionQueryService.movements(1L)).thenReturn(List.of());

		String view = controller.execution(1L, model);

		Assertions.assertThat(view).isEqualTo("app/execution-detail");
		Assertions.assertThat(model).containsEntry("execution", execution);
	}

	@Test
	void undoShouldDelegateToOrganizationServiceAndRenderDetail() {
		ExtendedModelMap model = new ExtendedModelMap();
		ExecutionResponse execution = execution();
		OrganizationUndoResponse undo = new OrganizationUndoResponse(1L, "FINISHED", 2, 2, 0, 0, "ok", List.of());

		when(executionQueryService.get(1L)).thenReturn(execution);
		when(organizationService.undo(1L)).thenReturn(undo);
		when(executionQueryService.steps(1L)).thenReturn(List.of());
		when(executionQueryService.errors(1L)).thenReturn(List.of());
		when(executionQueryService.movements(1L)).thenReturn(List.of());

		String view = controller.undo(1L, model);

		Assertions.assertThat(view).isEqualTo("app/execution-detail");
		Assertions.assertThat(model).containsEntry("execution", execution).containsEntry("undo", undo);
		verify(organizationService).undo(1L);
	}

	@Test
	void executionShouldRedirectToProgressWhenInventoryStillRunning() {
		ExtendedModelMap model = new ExtendedModelMap();
		ExecutionResponse execution = new ExecutionResponse(1L, "INVENTORY", "PROCESSING_FILES", NOW, null,
				"C:/media/input", null, 1, 1, 0, 0, 0, 0, null, null, "running", false);

		when(executionQueryService.get(1L)).thenReturn(execution);

		String view = controller.execution(1L, model);

		Assertions.assertThat(view)
				.isEqualTo("redirect:/app/progress/00000000-0000-7000-8000-000000000001?kind=inventory");
	}

	@Test
	void executionShouldRedirectToProgressWithOrganizationExecuteKindWhenStillRunning() {
		ExtendedModelMap model = new ExtendedModelMap();
		ExecutionResponse execution = new ExecutionResponse(2L, "ORGANIZATION", "SCANNING_FILES", NOW, null,
				"C:/media/input", "C:/media/output", 1, 1, 0, 0, 0, 0, null, null, "running", true);

		when(executionQueryService.get(2L)).thenReturn(execution);

		String view = controller.execution(2L, model);

		Assertions.assertThat(view)
				.isEqualTo("redirect:/app/progress/00000000-0000-7000-8000-000000000002?kind=organization-execute");
	}

	@Test
	void executionShouldRedirectToProgressWithOrganizationPreviewKindWhenStillRunning() {
		ExtendedModelMap model = new ExtendedModelMap();
		ExecutionResponse execution = new ExecutionResponse(3L, "ORGANIZATION", "STARTED", NOW, null, "C:/media/input",
				"C:/media/output", 0, 0, 0, 0, 0, 0, null, null, "running", false);

		when(executionQueryService.get(3L)).thenReturn(execution);

		String view = controller.execution(3L, model);

		Assertions.assertThat(view)
				.isEqualTo("redirect:/app/progress/00000000-0000-7000-8000-000000000003?kind=organization-preview");
	}

	private ExecutionResponse response(String type, String status, Boolean executeFlag) {
		return new ExecutionResponse(1L, type, status, NOW, NOW, "src", null, 1, 1, 0, 0, 0, 0, null, null, "ok",
				executeFlag);
	}

	private ExecutionResponse execution() {
		return new ExecutionResponse(1L, "INVENTORY", "FINISHED", NOW, NOW, "C:/media/input", null, 1, 1, 0, 0, 0, 0,
				null, null, "ok", false);
	}
}