package br.com.jorgemelo.nimbusfilemanager.metadata.application.family;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.classifier.MediaSubcategoryRule;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;

/**
 * Regular camera/phone output: {@code yyyyMMdd_HHmmss}, {@code yyyy-MM-dd ...}
 * or the {@code IMG_/VID_/DSC_/PXL_} prefixes. Subcategory-only family - the
 * capture date of these names is handled by the generic/dashed date families.
 *
 * <p>
 * Classification matches by name only (mirrors the former rule); the folder
 * signature is exposed for the organization rule.
 */
@Component
public class CameraMediaFamily implements MediaSubcategoryRule {

	private static final String ORDER = "060_CAMERA";

	private static final Pattern DATETIME = Pattern.compile("^\\d{8}_\\d{6}.*");
	private static final Pattern DASH = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}.*");

	public static boolean matchesName(String fileName) {
		if (fileName == null) {
			return false;
		}

		String value = fileName.toUpperCase(Locale.ROOT);

		return DATETIME.matcher(value).matches() || DASH.matcher(value).matches() || value.startsWith("IMG_")
				|| value.startsWith("VID_") || value.startsWith("DSC_") || value.startsWith("PXL_");
	}

	public static boolean matchesPath(String path) {
		return PathUtils.containsAnyFolder(path, MediaSubcategory.CAMERA.folderName());
	}

	@Override
	public boolean supports(String fileName, String path) {
		return matchesName(fileName);
	}

	@Override
	public MediaSubcategory subcategory() {
		return MediaSubcategory.CAMERA;
	}

	@Override
	public String name() {
		return ORDER;
	}
}