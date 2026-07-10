package br.com.jorgemelo.nimbusfilemanager.shared.util;

import java.util.Locale;

public final class SizeFormatter {

	private static final String[] UNITS = { "KB", "MB", "GB", "TB", "PB" };

	private SizeFormatter() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	public static String format(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		}

		double value = bytes;

		for (String unit : UNITS) {
			value = value / 1024;

			if (value < 1024) {
				return String.format(Locale.US, "%.2f %s", value, unit);
			}
		}

		return String.format(Locale.US, "%.2f EB", value / 1024);
	}
}