package br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto;

import java.util.Locale;

/**
 * Immutable GPS coordinates in decimal degrees.
 */
public record Coordinates(double latitude, double longitude) {

	public Coordinates {
		if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
			throw new IllegalArgumentException("Latitude out of range: " + latitude);
		}

		if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
			throw new IllegalArgumentException("Longitude out of range: " + longitude);
		}

		if (latitude == 0 && longitude == 0) {
			throw new IllegalArgumentException("Latitude and longitude cannot both be zero");
		}
	}

	public static Coordinates of(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			return null;
		}

		if (!Double.isFinite(latitude) || !Double.isFinite(longitude) || latitude < -90 || latitude > 90
				|| longitude < -180 || longitude > 180 || (latitude == 0 && longitude == 0)) {
			return null;
		}

		return new Coordinates(latitude, longitude);
	}

	/**
	 * Cache key with coordinates rounded to 4 decimal places (~11 m), so that
	 * near-identical coordinates share one cache entry.
	 */
	public String roundedKey() {
		return String.format(Locale.ROOT, "%.4f:%.4f", latitude, longitude);
	}
}