package br.com.jorgemelo.nimbusfilemanager.map.application.dto;

/**
 * Origin of a map pin's coordinate, so the UI can tell an exact capture point
 * from an approximate one.
 */
public enum MapPinSource {

	/** Real GPS coordinate read from the media's EXIF. */
	EXIF,

	/**
	 * Approximate point (representative of a municipality/state/country) used for
	 * media that only carries an administrative location, not raw coordinates.
	 */
	ADMINISTRATIVE
}