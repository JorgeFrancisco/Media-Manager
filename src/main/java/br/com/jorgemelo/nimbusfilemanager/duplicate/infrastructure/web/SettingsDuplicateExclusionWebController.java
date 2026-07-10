package br.com.jorgemelo.nimbusfilemanager.duplicate.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.jorgemelo.nimbusfilemanager.duplicate.application.DuplicateExclusionService;
import br.com.jorgemelo.nimbusfilemanager.duplicate.application.PhotoSimilarityService;
import br.com.jorgemelo.nimbusfilemanager.shared.i18n.LocalizedComponent;

/**
 * Restores files or folders to duplicate comparison, undoing an
 * "Excluir da comparação" done from the Duplicados screen. Both duplicate tabs
 * pick the restore up again: the exact queries re-filter on the next visit and
 * the similar cache is cleared so it recomputes with the item back in.
 */
@Controller
public class SettingsDuplicateExclusionWebController extends LocalizedComponent {

	private static final String ATTR_SUCCESS = "success";
	private static final String REDIRECT_SETTINGS = "redirect:/app/settings";

	private final DuplicateExclusionService duplicateExclusionService;
	private final PhotoSimilarityService photoSimilarityService;

	@Autowired
	public SettingsDuplicateExclusionWebController(DuplicateExclusionService duplicateExclusionService,
			PhotoSimilarityService photoSimilarityService) {
		this.duplicateExclusionService = duplicateExclusionService;
		this.photoSimilarityService = photoSimilarityService;
	}

	/** Restores a single file to duplicate comparison. */
	@PostMapping("/app/settings/duplicate-exclusions/file/remove")
	public String removeDuplicateFileExclusion(@RequestParam Long id, RedirectAttributes redirectAttributes) {
		duplicateExclusionService.removeFileExclusion(id);
		photoSimilarityService.invalidateCache();

		redirectAttributes.addFlashAttribute(ATTR_SUCCESS, message("backend.settings.dupExclRemoved"));

		return REDIRECT_SETTINGS;
	}

	/** Restores a whole folder (recursively) to duplicate comparison. */
	@PostMapping("/app/settings/duplicate-exclusions/folder/remove")
	public String removeDuplicateFolderExclusion(@RequestParam Long id, RedirectAttributes redirectAttributes) {
		duplicateExclusionService.removeFolderExclusion(id);
		photoSimilarityService.invalidateCache();

		redirectAttributes.addFlashAttribute(ATTR_SUCCESS, message("backend.settings.dupExclRemoved"));

		return REDIRECT_SETTINGS;
	}
}