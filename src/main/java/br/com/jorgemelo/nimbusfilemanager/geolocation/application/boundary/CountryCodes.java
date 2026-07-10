package br.com.jorgemelo.nimbusfilemanager.geolocation.application.boundary;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts geoBoundaries' ISO-3166-1 alpha-3 country codes (its {@code
 * shapeGroup}) to the alpha-2 codes the application stores, and resolves the
 * localized (pt-BR) country name from the JDK locale data - so "BRA" becomes
 * "BR" and "Brasil". Built once from the JDK's own ISO country tables; no
 * external dependency or bundled dataset.
 */
public final class CountryCodes {

	private static final Locale PT_BR = Locale.of("pt", "BR");

	private static final Map<String, String> ALPHA3_TO_ALPHA2 = buildAlpha3ToAlpha2();

	private CountryCodes() {
	}

	/** Every known ISO alpha-3 code mapped to its alpha-2 counterpart. */
	static Map<String, String> alpha3ToAlpha2() {
		return Map.copyOf(ALPHA3_TO_ALPHA2);
	}

	/** ISO alpha-3 (e.g. "BRA") to alpha-2 (e.g. "BR"); null when unknown. */
	static String alpha2(String alpha3) {
		if (alpha3 == null) {
			return null;
		}

		return ALPHA3_TO_ALPHA2.get(alpha3.strip().toUpperCase(Locale.ROOT));
	}

	/** Localized pt-BR country name for an alpha-2 code, or null. */
	public static String displayName(String alpha2) {
		if (alpha2 == null || alpha2.length() != 2) {
			return null;
		}

		String name = Locale.of("", alpha2.toUpperCase(Locale.ROOT)).getDisplayCountry(PT_BR);

		return name.isBlank() || name.equalsIgnoreCase(alpha2) ? null : name;
	}

	private static Map<String, String> buildAlpha3ToAlpha2() {
		Map<String, String> map = new HashMap<>();

		for (String alpha2 : Locale.getISOCountries()) {
			try {
				String alpha3 = Locale.of("", alpha2).getISO3Country();

				if (!alpha3.isBlank()) {
					map.put(alpha3.toUpperCase(Locale.ROOT), alpha2.toUpperCase(Locale.ROOT));
				}
			} catch (Exception _) {
				// Locale has no ISO3 for this territory; skip it.
			}
		}

		// geoBoundaries uses a few codes the JDK tables miss; map the common ones.
		map.putIfAbsent("XKX", "XK"); // Kosovo

		return map;
	}
}