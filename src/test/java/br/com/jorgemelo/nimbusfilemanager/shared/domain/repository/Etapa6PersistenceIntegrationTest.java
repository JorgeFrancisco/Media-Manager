package br.com.jorgemelo.nimbusfilemanager.shared.domain.repository;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.FingerprintKind;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model.FingerprintFailure;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model.MediaFingerprint;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.FingerprintFailureRepository;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.MediaFingerprintRepository;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.LocationProvider;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.model.MediaGeoLocation;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.model.ResolvedPlace;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.repository.MediaGeoLocationRepository;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.FileCategory;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.LocationConfidence;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.CatalogFile;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.CatalogFileLocation;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.MediaMetadata;
import jakarta.persistence.EntityManager;

/**
 * Persistence checks for Etapa 6:
 * <ul>
 * <li><b>D6</b> - deleting a catalog_file cascades to its fingerprints and
 * failures through the FK {@code ON DELETE CASCADE} (they are linked by a loose
 * id, not a managed JPA relationship), so no orphans remain.</li>
 * <li><b>D7</b> - a MediaGeoLocation is born with {@code manual = false} both
 * in Java (primitive default) and after a DB round-trip.</li>
 * </ul>
 */
@SpringBootTest
@Transactional
@Testcontainers
class Etapa6PersistenceIntegrationTest {

	private static final String ALGORITHM = "FFMPEG_LANCZOS_PHASH_256_V1";

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private CatalogFileRepository catalogFileRepository;

	@Autowired
	private MediaGeoLocationRepository mediaGeoLocationRepository;

	@Autowired
	private MediaFingerprintRepository mediaFingerprintRepository;

	@Autowired
	private FingerprintFailureRepository fingerprintFailureRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void deletingCatalogFileCascadesToFingerprintsAndFailures() {
		Long id = persistCatalogFile("cascade").getId();

		mediaFingerprintRepository.saveAndFlush(MediaFingerprint.builder().catalogFileId(id)
				.kind(FingerprintKind.PHOTO_PHASH).algorithm(ALGORITHM).sampleIndex(0).hashBytes(new byte[32])
				.sampleBytes(new byte[1024]).computedAt(LocalDateTime.now()).build());
		fingerprintFailureRepository.saveAndFlush(FingerprintFailure.builder().catalogFileId(id)
				.kind(FingerprintKind.PHOTO_PHASH).algorithm(ALGORITHM).attempts(1).build());

		Assertions.assertThat(mediaFingerprintRepository.existsByCatalogFileIdAndKindAndAlgorithmAndSampleIndex(id,
				FingerprintKind.PHOTO_PHASH, ALGORITHM, 0)).isTrue();
		Assertions.assertThat(fingerprintFailureRepository.findByCatalogFileIdAndKindAndAlgorithm(id,
				FingerprintKind.PHOTO_PHASH, ALGORITHM)).isPresent();

		catalogFileRepository.deleteById(id);
		catalogFileRepository.flush();

		Assertions.assertThat(mediaFingerprintRepository.existsByCatalogFileIdAndKindAndAlgorithmAndSampleIndex(id,
				FingerprintKind.PHOTO_PHASH, ALGORITHM, 0)).as("fingerprint cascaded away").isFalse();
		Assertions.assertThat(fingerprintFailureRepository.findByCatalogFileIdAndKindAndAlgorithm(id,
				FingerprintKind.PHOTO_PHASH, ALGORITHM)).as("failure cascaded away").isEmpty();
	}

	@Test
	void exhaustedFingerprintFailuresExposeCurrentPathAndError() {
		CatalogFile file = persistCatalogFile("failure-details");

		fingerprintFailureRepository.saveAndFlush(FingerprintFailure.builder().catalogFileId(file.getId())
				.kind(FingerprintKind.PHOTO_PHASH).algorithm(ALGORITHM).attempts(3).lastError("decode failed").build());

		Assertions.assertThat(fingerprintFailureRepository.findExhaustedWithPath(FingerprintKind.PHOTO_PHASH, ALGORITHM,
				3, "[unsupported] ")).anySatisfy(failure -> {
					Assertions.assertThat(failure.path()).isEqualTo(file.getLocation().getCurrentPath());
					Assertions.assertThat(failure.error()).isEqualTo("decode failed");
				});
	}

	@Test
	void newMediaGeoLocationIsBornWithManualFalseInJavaAndAfterPersistence() {
		// Java default (primitive boolean): manual is false without setting it.
		Assertions.assertThat(MediaGeoLocation.builder().build().isManual()).isFalse();

		// Persist media_metadata through the CatalogFile aggregate (cascade ALL +
		// @MapsId),
		// not via a dedicated repository - mirrors how the inventory mapper persists
		// it.
		// media_geo_location's FK targets media_metadata(catalog_file_id), so the cascade
		// must create that row for the insert below to satisfy the FK.
		CatalogFile file = CatalogFile.builder().fileKey("etapa6-manual-" + System.nanoTime()).fileName("manual.jpg")
				.extension("jpg").sizeBytes(1L).modifiedAt(LocalDateTime.now()).fileType(FileType.PHOTO).build();
		file.setMetadata(MediaMetadata.builder().catalogFile(file).category(FileCategory.MEDIA)
				.subcategory(MediaSubcategory.CAMERA).build());

		catalogFileRepository.saveAndFlush(file);

		mediaGeoLocationRepository
				.saveAndFlush(MediaGeoLocation.builder().id(file.getId())
						.place(ResolvedPlace.builder().confidence(LocationConfidence.HIGH)
								.provider(LocationProvider.ADMIN_BOUNDARIES).resolvedAt(LocalDateTime.now()).build())
						.build());

		// Fresh read from the DB (context cleared): confirms false was persisted.
		entityManager.clear();

		Assertions.assertThat(mediaGeoLocationRepository.findById(file.getId())).get()
				.extracting(MediaGeoLocation::isManual).isEqualTo(false);
	}

	private CatalogFile persistCatalogFile(String key) {
		String path = "C:/test/" + key + "-" + System.nanoTime() + ".jpg";

		CatalogFile file = CatalogFile.builder().fileKey("etapa6-" + key + "-" + System.nanoTime()).fileName(key + ".jpg")
				.extension("jpg").sizeBytes(1L).modifiedAt(LocalDateTime.now()).fileType(FileType.PHOTO).build();
		file.setLocation(CatalogFileLocation.builder().catalogFile(file).currentPath(path).currentFolder("C:/test")
				.originalPath(path).originalFolder("C:/test").build());

		return catalogFileRepository.saveAndFlush(file);
	}
}