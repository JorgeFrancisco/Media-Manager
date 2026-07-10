package br.com.jorgemelo.nimbusfilemanager.organization.application.resolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationItem;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationConflictType;

class OrganizationConflictDetectorTest {

	@TempDir
	Path tempDir;

	private final OrganizationConflictDetector detector = new OrganizationConflictDetector();

	@Test
	void shouldDetectDuplicateTargetsAndExistingTarget() throws Exception {
		Path existingSource = Files.writeString(tempDir.resolve("a.jpg"), "source");
		Path existingTarget = Files.writeString(tempDir.resolve("existing.jpg"), "content");
		Path duplicateTarget = tempDir.resolve("duplicate.jpg");
		Path sourceB = Files.writeString(tempDir.resolve("b.jpg"), "b");
		Path sourceC = Files.writeString(tempDir.resolve("c.jpg"), "c");

		List<OrganizationItem> result = detector.detect(List.of(item("a.jpg", existingSource, existingTarget, false),
				item("b.jpg", sourceB, duplicateTarget, false), item("c.jpg", sourceC, duplicateTarget, false)));

		Assertions.assertThat(result.get(0).targetExists()).isTrue();
		Assertions.assertThat(result.get(0).conflictType()).isEqualTo(OrganizationConflictType.TARGET_EXISTS.name());

		Assertions.assertThat(result.get(1).duplicateTarget()).isTrue();
		Assertions.assertThat(result.get(1).conflictType()).isEqualTo(OrganizationConflictType.DUPLICATE_TARGET.name());

		Assertions.assertThat(result.get(2).duplicateTarget()).isTrue();
		Assertions.assertThat(result.get(2).conflict()).isTrue();
	}

	@Test
	void shouldNotFlagExistingTargetAsConflictWhenSourceIsAlreadyGone() throws Exception {
		// Re-run / already-moved case: the source no longer exists on disk but the
		// target does. This must NOT be a blocking conflict - moveOne() handles it
		// per item (ALREADY_MOVED skip or SOURCE_NOT_FOUND), keeping re-runs possible.
		Path missingSource = tempDir.resolve("gone.jpg");
		Path existingTarget = Files.writeString(tempDir.resolve("target.jpg"), "content");

		OrganizationItem result = detector.detect(List.of(item("gone.jpg", missingSource, existingTarget, false)))
				.get(0);

		Assertions.assertThat(result.targetExists()).isFalse();
		Assertions.assertThat(result.conflict()).isFalse();
		Assertions.assertThat(result.conflictType()).isNull();
	}

	@Test
	void shouldIgnoreConflictsWhenSourceAndTargetAreSamePath() {
		Path path = tempDir.resolve("same.jpg");

		OrganizationItem result = detector.detect(List.of(item("same.jpg", path, path, true))).get(0);

		Assertions.assertThat(result.conflict()).isFalse();
		Assertions.assertThat(result.targetExists()).isFalse();
		Assertions.assertThat(result.duplicateTarget()).isFalse();
	}

	private OrganizationItem item(String fileName, Path source, Path target, boolean samePath) {
		return new OrganizationItem(1L, fileName, source.toString(), target.toString(), "202405", "09", "MEDIA",
				"CAMERA", "PHOTO", "CAMERA", "FILE_NAME", 100L, samePath, false, false, false, false, null);
	}
}