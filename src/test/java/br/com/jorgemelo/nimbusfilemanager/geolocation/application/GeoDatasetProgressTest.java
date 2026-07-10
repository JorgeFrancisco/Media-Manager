package br.com.jorgemelo.nimbusfilemanager.geolocation.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto.Snapshot;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.AdminBoundaryKind;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.Phase;

class GeoDatasetProgressTest {

	@Test
	void shouldStartIdleWithNoPercentOrEta() {
		GeoDatasetProgress progress = new GeoDatasetProgress();

		Snapshot snapshot = progress.snapshot();

		Assertions.assertThat(snapshot.phase()).isEqualTo(Phase.IDLE);
		Assertions.assertThat(snapshot.downloading()).isFalse();
		Assertions.assertThat(snapshot.importing()).isFalse();
		Assertions.assertThat(snapshot.percent()).isEqualTo(-1);
		Assertions.assertThat(snapshot.etaSeconds()).isEqualTo(-1);
	}

	@Test
	void shouldComputeDownloadPercentFromKnownTotal() {
		GeoDatasetProgress progress = new GeoDatasetProgress();

		progress.startDownload(AdminBoundaryKind.MUNICIPALITY, 1000);
		progress.addDownloadedBytes(250);

		Snapshot snapshot = progress.snapshot();

		Assertions.assertThat(snapshot.downloading()).isTrue();
		Assertions.assertThat(snapshot.stepLabel()).isEqualTo("settings.geo.step.municipality");
		Assertions.assertThat(snapshot.percent()).isEqualTo(25);
		Assertions.assertThat(snapshot.bytesDone()).isEqualTo(250);
		Assertions.assertThat(snapshot.bytesTotal()).isEqualTo(1000);
	}

	@Test
	void shouldReportUnknownPercentWhenContentLengthIsMissing() {
		GeoDatasetProgress progress = new GeoDatasetProgress();

		progress.startDownload(AdminBoundaryKind.COUNTRY, -1);
		progress.addDownloadedBytes(500);

		Snapshot snapshot = progress.snapshot();

		Assertions.assertThat(snapshot.downloading()).isTrue();
		Assertions.assertThat(snapshot.percent()).isEqualTo(-1);
		Assertions.assertThat(snapshot.etaSeconds()).isEqualTo(-1);
		Assertions.assertThat(snapshot.bytesDone()).isEqualTo(500);
	}

	@Test
	void shouldCapPercentAtOneHundredWhenServerUnderreportsContentLength() {
		GeoDatasetProgress progress = new GeoDatasetProgress();

		progress.startDownload(AdminBoundaryKind.STATE, 100);
		progress.addDownloadedBytes(250);

		Assertions.assertThat(progress.snapshot().percent()).isEqualTo(100);
	}

	@Test
	void shouldAccumulateImportedRecordsAcrossLevels() {
		GeoDatasetProgress progress = new GeoDatasetProgress();

		progress.startImport(AdminBoundaryKind.COUNTRY, -1);
		progress.addImportedRecords(200);
		progress.startImport(AdminBoundaryKind.MUNICIPALITY, -1);
		progress.addImportedRecords(300);

		Snapshot snapshot = progress.snapshot();

		Assertions.assertThat(snapshot.importing()).isTrue();
		Assertions.assertThat(snapshot.stepLabel()).isEqualTo("settings.geo.step.municipality");
		Assertions.assertThat(snapshot.recordsImported()).isEqualTo(500);
		Assertions.assertThat(snapshot.percent()).isEqualTo(-1);
	}

	@Test
	void shouldComputeImportPercentFromConsumedBytes() {
		GeoDatasetProgress progress = new GeoDatasetProgress();

		progress.startImport(AdminBoundaryKind.MUNICIPALITY, 2000);
		progress.addImportedBytes(500);
		progress.addImportedRecords(120);

		Snapshot snapshot = progress.snapshot();

		Assertions.assertThat(snapshot.importing()).isTrue();
		Assertions.assertThat(snapshot.percent()).isEqualTo(25);
		Assertions.assertThat(snapshot.recordsImported()).isEqualTo(120);
	}

	@Test
	void resetShouldClearPreviousOperation() {
		GeoDatasetProgress progress = new GeoDatasetProgress();

		progress.startDownload(AdminBoundaryKind.MUNICIPALITY, 1000);
		progress.addDownloadedBytes(999);
		progress.startImport(AdminBoundaryKind.MUNICIPALITY, 1000);
		progress.addImportedRecords(42);
		progress.reset();

		Snapshot snapshot = progress.snapshot();

		Assertions.assertThat(snapshot.phase()).isEqualTo(Phase.IDLE);
		Assertions.assertThat(snapshot.bytesDone()).isZero();
		Assertions.assertThat(snapshot.recordsImported()).isZero();
		Assertions.assertThat(snapshot.stepLabel()).isEmpty();
	}
}