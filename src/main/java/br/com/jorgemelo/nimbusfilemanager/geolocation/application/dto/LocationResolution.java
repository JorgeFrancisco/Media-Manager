package br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto;

import java.time.LocalDateTime;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.LocationProvider;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.LocationConfidence;

/**
 * Result of a reverse-geocoding resolution, provider-agnostic. This is what
 * gets persisted as {@code media_geo_location} and cached.
 */
public record LocationResolution(String countryCode, String countryName, String stateName, String cityName,
		Double distanceKm, LocationConfidence confidence, LocationProvider provider, String datasetVersion,
		LocalDateTime resolvedAt) {
}