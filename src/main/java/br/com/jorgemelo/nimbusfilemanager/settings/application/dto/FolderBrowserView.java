package br.com.jorgemelo.nimbusfilemanager.settings.application.dto;

import java.util.List;

public record FolderBrowserView(String currentPath, String parentPath, List<FolderBrowserEntry> directories,
		boolean truncated) {
}