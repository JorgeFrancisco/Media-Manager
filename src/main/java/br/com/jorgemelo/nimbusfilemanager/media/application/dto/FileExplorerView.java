package br.com.jorgemelo.nimbusfilemanager.media.application.dto;

import java.util.List;

public record FileExplorerView(

		String path,

		String parentPath,

		String viewMode,

		boolean exists,

		boolean directory,

		boolean accessDenied,

		int folderCount,

		int fileCount,

		int missingCount,

		int inventoriedCount,

		int page,

		int size,

		int totalItems,

		int totalPages,

		boolean hasPrevious,

		boolean hasNext,

		List<FileExplorerEntry> entries) {
}