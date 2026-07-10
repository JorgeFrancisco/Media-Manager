package br.com.jorgemelo.nimbusfilemanager.inventory.infrastructure.web;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.jorgemelo.nimbusfilemanager.inventory.application.batch.InventoryBatchLauncherService;
import br.com.jorgemelo.nimbusfilemanager.inventory.application.dto.InventoryRequest;
import br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.InventoryWatchService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.AppSettingService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionTrigger;
import br.com.jorgemelo.nimbusfilemanager.shared.i18n.LocalizedComponent;

/**
 * First-run wizard: asks which folder to watch (plus the same options the old
 * manual "Inventario" screen exposed) once, persists the choice as app
 * settings, and kicks off the first scan. DashboardWebController redirects here
 * whenever {@link AppSettingService#WATCH_FOLDER} is still unconfigured, so
 * every fresh install goes through this before seeing the dashboard.
 */
@Controller
public class OnboardingWebController extends LocalizedComponent {

	private final AppSettingService appSettingService;
	private final InventoryBatchLauncherService inventoryBatchLauncherService;
	private final InventoryWatchService inventoryWatchService;

	@Autowired
	public OnboardingWebController(AppSettingService appSettingService,
			InventoryBatchLauncherService inventoryBatchLauncherService, InventoryWatchService inventoryWatchService) {
		this.appSettingService = appSettingService;
		this.inventoryBatchLauncherService = inventoryBatchLauncherService;
		this.inventoryWatchService = inventoryWatchService;
	}

	@GetMapping("/app/onboarding")
	public String onboarding() {
		if (!isConfigured()) {
			return "app/onboarding";
		}

		return "redirect:/app";
	}

	@PostMapping("/app/onboarding")
	public String configure(@RequestParam String sourcePath, @RequestParam(defaultValue = "false") boolean recursive,
			@RequestParam(defaultValue = "false") boolean includeHidden,
			@RequestParam(defaultValue = "false") boolean calculateHashes,
			@RequestParam(defaultValue = "false") boolean forceAnalysis, Authentication authentication, Model model) {
		model.addAttribute("sourcePath", sourcePath);
		model.addAttribute("recursive", recursive);
		model.addAttribute("includeHidden", includeHidden);
		model.addAttribute("calculateHashes", calculateHashes);
		model.addAttribute("forceAnalysis", forceAnalysis);

		String validationError = validateSourcePath(sourcePath);

		if (validationError != null) {
			model.addAttribute("error", validationError);

			return "app/onboarding";
		}

		String username = authentication != null ? authentication.getName() : null;

		appSettingService.update(SettingsConstants.WATCH_RECURSIVE, Boolean.toString(recursive), username);
		appSettingService.update(SettingsConstants.WATCH_INCLUDE_HIDDEN, Boolean.toString(includeHidden), username);
		appSettingService.update(SettingsConstants.WATCH_CALCULATE_HASHES, Boolean.toString(calculateHashes), username);
		appSettingService.update(SettingsConstants.WATCH_FORCE_ANALYSIS, Boolean.toString(forceAnalysis), username);

		// Written last: DashboardWebController treats a non-blank WATCH_FOLDER as
		// "onboarding done",
		// so every other setting above must already be saved by the time this one
		// lands.
		appSettingService.update(SettingsConstants.WATCH_FOLDER, sourcePath, username);

		inventoryWatchService.reconfigure();

		var request = new InventoryRequest(sourcePath, recursive, includeHidden, calculateHashes, forceAnalysis);
		var started = inventoryBatchLauncherService.launch(request, ExecutionTrigger.MANUAL);

		return "redirect:/app/progress/" + started.executionId() + "?kind=inventory";
	}

	private boolean isConfigured() {
		return !appSettingService.stringValue(SettingsConstants.WATCH_FOLDER, "").isBlank();
	}

	private String validateSourcePath(String sourcePath) {
		if (sourcePath == null || sourcePath.isBlank()) {
			return message("backend.onboarding.folderRequired");
		}

		Path path = Path.of(sourcePath).toAbsolutePath().normalize();

		if (!Files.isDirectory(path)) {
			return message("backend.onboarding.folderInvalid", path);
		}

		return null;
	}
}