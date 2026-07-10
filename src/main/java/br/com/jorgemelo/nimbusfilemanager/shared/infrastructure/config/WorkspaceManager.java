package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config;

import java.nio.file.Path;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.shared.application.constants.WorkspaceFolders;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.NimbusFileManagerProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;

@Component
public class WorkspaceManager {

	private final NimbusFileManagerProperties properties;

	public WorkspaceManager(NimbusFileManagerProperties properties) {
		this.properties = properties;
	}

	public Path getWorkspacePath() {
		return PathUtils.normalizePath(properties.workspace());
	}

	public Path resolve(String... paths) {
		Path result = getWorkspacePath();

		for (String path : paths) {
			result = result.resolve(path);
		}

		return result.normalize();
	}

	public Path temp() {
		return resolve(WorkspaceFolders.TEMP);
	}

	public Path geodata() {
		return resolve(WorkspaceFolders.GEODATA);
	}
}