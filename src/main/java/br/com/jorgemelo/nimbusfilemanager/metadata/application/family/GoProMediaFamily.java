package br.com.jorgemelo.nimbusfilemanager.metadata.application.family;

import java.util.Locale;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.classifier.MediaSubcategoryRule;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;

/**
 * GoPro output ({@code GOPR/GH/GX} prefixes) or a GoPro folder.
 * Subcategory-only family; capture date falls to the generic/dashed date
 * families.
 */
@Component
public class GoProMediaFamily implements MediaSubcategoryRule {

	private static final String ORDER = "050_GOPRO";

	public static boolean matchesName(String fileName) {
		if (fileName == null) {
			return false;
		}

		String value = fileName.toUpperCase(Locale.ROOT);

		return value.startsWith("GOPR") || value.startsWith("GH") || value.startsWith("GX");
	}

	public static boolean matchesPath(String path) {
		return PathUtils.containsAnyFolder(path, MediaSubcategory.GOPRO.folderName());
	}

	@Override
	public boolean supports(String fileName, String path) {
		return matchesName(fileName) || matchesPath(path);
	}

	@Override
	public MediaSubcategory subcategory() {
		return MediaSubcategory.GOPRO;
	}

	@Override
	public String name() {
		return ORDER;
	}
}