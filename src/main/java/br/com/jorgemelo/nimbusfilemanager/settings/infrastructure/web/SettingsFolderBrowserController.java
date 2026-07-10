package br.com.jorgemelo.nimbusfilemanager.settings.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.jorgemelo.nimbusfilemanager.settings.application.FolderBrowserService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.dto.FolderBrowserView;

@RestController
public class SettingsFolderBrowserController {

	private final FolderBrowserService folderBrowserService;

	public SettingsFolderBrowserController(FolderBrowserService folderBrowserService) {
		this.folderBrowserService = folderBrowserService;
	}

	@GetMapping("/app/settings/folders")
	public FolderBrowserView folders(@RequestParam(required = false) String path) {
		try {
			return folderBrowserService.browse(path);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}
}