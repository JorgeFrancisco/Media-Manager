package br.com.jorgemelo.nimbusfilemanager.inventory.application.dto;

import java.util.List;

public record ScanOptions(boolean recursive, boolean includeHidden, List<String> includeExtensions,
		List<String> excludeExtensions, List<String> excludeFolders) {

	public ScanOptions(boolean recursive, boolean includeHidden, List<String> includeExtensions,
			List<String> excludeExtensions) {
		this(recursive, includeHidden, includeExtensions, excludeExtensions, List.of());
	}

	public static ScanOptions defaultOptions() {
		return new ScanOptions(true, false, List.of(), List.of("tmp", "temp", "ini", "db", "lnk", "part", "crdownload"),
				List.of(".git", ".svn", ".hg", "node_modules", "target", "build", "out"));
	}
}