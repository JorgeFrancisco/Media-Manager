package br.com.jorgemelo.nimbusfilemanager.geolocation.application.constants;

/**
 * Contract data constants for the geolocation domain: the page/preference keys
 * that remember the last-picked location-rebuild scope, shared between the
 * geodata actions controller and the read-side settings model.
 */
public final class GeolocationConstants {

	public static final String GEO_PAGE_KEY = "geodata";
	public static final String GEO_REBUILD_SCOPE_KEY = "rebuildScope";

	private GeolocationConstants() {
	}
}