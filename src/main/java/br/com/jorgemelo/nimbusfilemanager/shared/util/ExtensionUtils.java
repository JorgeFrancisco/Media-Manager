package br.com.jorgemelo.nimbusfilemanager.shared.util;

import java.nio.file.Path;
import java.util.Locale;

public final class ExtensionUtils {

	private ExtensionUtils() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	public static String normalize(String extension) {
		if (extension == null) {
			return "";
		}

		String value = extension.trim().toLowerCase(Locale.ROOT);

		return value.startsWith(".") ? value.substring(1) : value;
	}

	public static String fromPath(Path path) {
		if (path == null || path.getFileName() == null) {
			return "";
		}

		String fileName = path.getFileName().toString();

		int index = fileName.lastIndexOf('.');

		if (index < 0 || index == fileName.length() - 1) {
			return "";
		}

		return normalize(fileName.substring(index + 1));
	}
}