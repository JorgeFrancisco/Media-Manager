package br.com.jorgemelo.nimbusfilemanager.media.application.dto;

import java.nio.file.Path;

public record BrowsedFolder(Path path, String parentPath, String viewMode, boolean exists, boolean directory,
		boolean accessDenied, int folderCount, int fileCount, int missingCount, int inventoriedCount) {
}