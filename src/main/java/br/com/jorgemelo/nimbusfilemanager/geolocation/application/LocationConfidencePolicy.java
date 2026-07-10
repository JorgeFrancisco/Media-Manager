package br.com.jorgemelo.nimbusfilemanager.geolocation.application;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.AdminBoundaryKind;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.LocationConfidence;

/**
 * Centralizes how confidence is assigned under the administrative-containment
 * model (Point-in-Polygon): confidence reflects the finest administrative level
 * that actually contains the point. This is the single place in the application
 * that knows these rules - it never knows about UI.
 */
@Component
public class LocationConfidencePolicy {

	/**
	 * The deeper the level that contains the point, the higher the confidence. A
	 * municipal containment is unambiguous (VERY_HIGH); a state-only match is
	 * partial (MEDIUM); a country-only match is weak (LOW).
	 */
	public LocationConfidence confidenceForKind(AdminBoundaryKind kind) {
		if (kind == null) {
			return LocationConfidence.VERY_LOW;
		}

		return switch (kind) {
		case MUNICIPALITY -> LocationConfidence.VERY_HIGH;
		case STATE -> LocationConfidence.MEDIUM;
		case COUNTRY -> LocationConfidence.LOW;
		};
	}

	/**
	 * A location resolved by the nearest-boundary fallback is an approximation: the
	 * point itself is outside every polygon (typically at sea near the coast), so
	 * it never rates above LOW regardless of the level found.
	 */
	public LocationConfidence confidenceForNearestFallback() {
		return LocationConfidence.LOW;
	}
}