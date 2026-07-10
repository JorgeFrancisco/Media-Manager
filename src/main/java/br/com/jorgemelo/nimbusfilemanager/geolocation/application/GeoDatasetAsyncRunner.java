package br.com.jorgemelo.nimbusfilemanager.geolocation.application;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto.OfflineGeoDatasetStatus;
import br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto.Snapshot;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.AsyncConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs dataset download/import in the background so the admin request returns
 * immediately (the import of hundreds of thousands of localities takes a few
 * minutes). Lives in its own bean so the {@code @Async} proxy is honored.
 */
@Slf4j
@Service
public class GeoDatasetAsyncRunner {

	private final OfflineGeoDataset dataset;
	private final MediaLocationService mediaLocationService;
	private final GeoDatasetProgress progress;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicReference<String> lastError = new AtomicReference<>();
	private final AtomicReference<OfflineGeoDatasetStatus> lastResult = new AtomicReference<>();

	public GeoDatasetAsyncRunner(OfflineGeoDataset dataset, MediaLocationService mediaLocationService,
			GeoDatasetProgress progress) {
		this.dataset = dataset;
		this.mediaLocationService = mediaLocationService;
		this.progress = progress;
	}

	/** @return false when a download/import is already in progress. */
	public boolean start() {
		if (!running.compareAndSet(false, true)) {
			return false;
		}

		lastError.set(null);
		lastResult.set(null);
		progress.reset();

		return true;
	}

	@Async(AsyncConfig.GEOLOCATION_EXECUTOR)
	public void downloadAndImport() {
		try {
			OfflineGeoDatasetStatus result = dataset.downloadAndImport();

			// A new dataset version invalidates previous resolutions; clear
			// the persistent cache so rebuilds resolve against fresh data.
			mediaLocationService.clearCache();
			lastResult.set(result);
		} catch (Exception e) {
			log.error("Geographic dataset download/import failed", e);

			lastError.set(e.getMessage());
		} finally {
			running.set(false);
		}
	}

	public boolean isRunning() {
		return running.get();
	}

	/** Progress of the operation in course, for the admin screen. */
	public Snapshot progress() {
		return progress.snapshot();
	}

	public String lastError() {
		return lastError.get();
	}

	public OfflineGeoDatasetStatus lastResult() {
		return lastResult.get();
	}
}