package br.com.jorgemelo.nimbusfilemanager.execution.infrastructure.web;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionQueryService;
import br.com.jorgemelo.nimbusfilemanager.execution.application.dto.ExecutionResponse;
import br.com.jorgemelo.nimbusfilemanager.organization.application.OrganizationService;

@Controller
public class ExecutionWebController {

	/**
	 * Statuses an execution can still be in while its background thread is running.
	 * Opening "/app/executions/{id}" for one of these used to render
	 * execution-detail.html with incomplete data (and could blow up outright); now
	 * it sends the user to the live progress screen instead, which is also how you
	 * get back to watching a run you navigated away from.
	 */
	private static final Set<String> IN_PROGRESS_STATUSES = Set.of("STARTED", "SCANNING_FILES", "PROCESSING_FILES");
	private static final String ATTR_EXECUTION = "execution";
	private static final String ATTR_STEPS = "steps";
	private static final String ATTR_ERRORS = "errors";
	private static final String ATTR_MOVEMENTS = "movements";
	private static final String ATTR_MOVEMENT_SUMMARY = "movementSummary";
	private static final String VIEW_EXECUTION_DETAIL = "app/execution-detail";

	private final ExecutionQueryService executionQueryService;
	private final OrganizationService organizationService;

	public ExecutionWebController(ExecutionQueryService executionQueryService,
			OrganizationService organizationService) {
		this.executionQueryService = executionQueryService;
		this.organizationService = organizationService;
	}

	@GetMapping("/app/executions/{id}")
	public String execution(@PathVariable UUID id, Model model) {
		ExecutionResponse execution = executionQueryService.get(id);

		if (IN_PROGRESS_STATUSES.contains(execution.status())) {
			return "redirect:/app/progress/" + id + "?kind=" + progressKind(execution);
		}

		model.addAttribute(ATTR_EXECUTION, execution);
		model.addAttribute(ATTR_STEPS, executionQueryService.steps(id));
		model.addAttribute(ATTR_ERRORS, executionQueryService.errors(id));
		model.addAttribute(ATTR_MOVEMENTS, executionQueryService.movements(id));
		model.addAttribute(ATTR_MOVEMENT_SUMMARY, executionQueryService.movementSummary(id));

		return VIEW_EXECUTION_DETAIL;
	}

	private String progressKind(ExecutionResponse execution) {
		if (!"ORGANIZATION".equals(execution.executionType())) {
			return "inventory";
		}

		return Boolean.TRUE.equals(execution.executeFlag()) ? "organization-execute" : "organization-preview";
	}

	@PostMapping("/app/executions/{id}/undo")
	public String undo(@PathVariable UUID id, Model model) {
		model.addAttribute(ATTR_EXECUTION, executionQueryService.get(id));
		model.addAttribute("undo", organizationService.undoPublic(id));
		model.addAttribute(ATTR_STEPS, executionQueryService.steps(id));
		model.addAttribute(ATTR_ERRORS, executionQueryService.errors(id));
		model.addAttribute(ATTR_MOVEMENTS, executionQueryService.movements(id));
		model.addAttribute(ATTR_MOVEMENT_SUMMARY, executionQueryService.movementSummary(id));

		return VIEW_EXECUTION_DETAIL;
	}

	public String execution(Long id, Model model) {
		ExecutionResponse execution = executionQueryService.get(id);
		if (IN_PROGRESS_STATUSES.contains(execution.status())) {
			return "redirect:/app/progress/" + execution.executionId() + "?kind=" + progressKind(execution);
		}

		model.addAttribute(ATTR_EXECUTION, execution);
		model.addAttribute(ATTR_STEPS, executionQueryService.steps(id));
		model.addAttribute(ATTR_ERRORS, executionQueryService.errors(id));
		model.addAttribute(ATTR_MOVEMENTS, executionQueryService.movements(id));
		model.addAttribute(ATTR_MOVEMENT_SUMMARY, executionQueryService.movementSummary(id));

		return VIEW_EXECUTION_DETAIL;
	}

	public String undo(Long id, Model model) {
		model.addAttribute(ATTR_EXECUTION, executionQueryService.get(id));
		model.addAttribute("undo", organizationService.undo(id));
		model.addAttribute(ATTR_STEPS, executionQueryService.steps(id));
		model.addAttribute(ATTR_ERRORS, executionQueryService.errors(id));
		model.addAttribute(ATTR_MOVEMENTS, executionQueryService.movements(id));
		model.addAttribute(ATTR_MOVEMENT_SUMMARY, executionQueryService.movementSummary(id));

		return VIEW_EXECUTION_DETAIL;
	}
}