package br.com.jorgemelo.nimbusfilemanager.geolocation.application;

import br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto.OfflineGeoDatasetStatus;

/**
 * Administrative operations over the offline geographic dataset, exposed to the
 * admin UI without revealing the concrete source. Download/update/remove are
 * admin-only actions; resolution itself is fully offline.
 */
public interface OfflineGeoDataset {

	OfflineGeoDatasetStatus status();

	/** Downloads (or re-downloads) and imports the dataset. Blocking. */
	OfflineGeoDatasetStatus downloadAndImport();

	/** Removes downloaded files, extracted files and imported records. */
	void remove();
}