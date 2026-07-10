package br.com.jorgemelo.nimbusfilemanager.geolocation.application;

import java.util.ArrayList;
import java.util.List;

/**
 * Display helpers for resolved locations, shared by every screen (photo and
 * video details, timeline, previews). Keeps user-facing place formats in one
 * place: "Curitiba, Paraná, Brasil", "Curitiba, Paraná".
 */
public final class LocationDisplay {

	private LocationDisplay() {
	}

	/** "Curitiba, Paraná, Brasil" (skips blank parts), or null. */
	public static String fullLabel(String cityName, String stateName, String countryName) {
		List<String> parts = new ArrayList<>();

		add(parts, cityName);
		add(parts, stateName);
		add(parts, countryName);

		return parts.isEmpty() ? null : String.join(", ", parts);
	}

	/** Compact card label: "Curitiba, Paraná", falling back to country. */
	public static String shortLabel(String cityName, String stateName, String countryName) {
		List<String> parts = new ArrayList<>();

		add(parts, cityName);
		add(parts, stateName);

		if (parts.isEmpty()) {
			add(parts, countryName);
		}

		return parts.isEmpty() ? null : String.join(", ", parts);
	}

	private static void add(List<String> parts, String value) {
		if (value != null && !value.isBlank()) {
			parts.add(value.strip());
		}
	}
}