package br.com.jorgemelo.nimbusfilemanager.metadata.application.family;

import java.util.Locale;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.classifier.MediaSubcategoryRule;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;

/**
 * DJI drone output ({@code DJI_...}) or a DJI/Drone folder. Subcategory-only
 * family; capture date falls to the generic/dashed date families.
 */
@Component
public class DroneMediaFamily implements MediaSubcategoryRule {

	private static final String ORDER = "040_DRONE";

	public static boolean matchesName(String fileName) {
		if (fileName == null) {
			return false;
		}

		return fileName.toUpperCase(Locale.ROOT).startsWith("DJI_");
	}

	public static boolean matchesPath(String path) {
		return PathUtils.containsAnyFolder(path, MediaSubcategory.DRONE.folderName(), "DJI");
	}

	@Override
	public boolean supports(String fileName, String path) {
		return matchesName(fileName) || matchesPath(path);
	}

	@Override
	public MediaSubcategory subcategory() {
		return MediaSubcategory.DRONE;
	}

	@Override
	public String name() {
		return ORDER;
	}
}