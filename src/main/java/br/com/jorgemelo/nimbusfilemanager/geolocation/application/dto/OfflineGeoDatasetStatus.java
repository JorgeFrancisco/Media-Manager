package br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto;

import java.time.LocalDateTime;

/**
 * Snapshot of the offline geographic dataset for the admin screen:
 * availability, provider label, license/attribution, version, size on disk,
 * imported record/polygon count and last error (if any). Technology-neutral: it
 * never names a concrete source.
 */
public record OfflineGeoDatasetStatus(boolean available, String version, long importedRecords, long sizeBytes,
		LocalDateTime downloadedAt, LocalDateTime importedAt, String directory, String lastError, String provider,
		String license) {

	public static OfflineGeoDatasetStatus unavailable(String directory, String lastError) {
		return new OfflineGeoDatasetStatus(false, null, 0, 0, null, null, directory, lastError, null, null);
	}
}