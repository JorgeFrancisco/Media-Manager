package br.com.jorgemelo.nimbusfilemanager.organization.application;

import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.FileHashService;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.MoveBaseline;

class OrganizationMoveVerifierTest {

	@TempDir
	Path tempDir;

	private final FileHashService fileHashService = new FileHashService();

	private final OrganizationMoveVerifier verifier = new OrganizationMoveVerifier(fileHashService);

	@Test
	void captureReadsSizeAndSha256OfTheSource() throws Exception {
		Path source = Files.writeString(tempDir.resolve("source.jpg"), "hello");

		MoveBaseline baseline = verifier.capture(source);

		Assertions.assertThat(baseline.sizeBytes()).isEqualTo(5);
		Assertions.assertThat(baseline.sha256()).isEqualTo(fileHashService.sha256(source));
	}

	@Test
	void verifyPassesWhenTargetMatchesBaselineAndSourceIsGone() throws Exception {
		Path source = tempDir.resolve("source.jpg");
		Path target = Files.writeString(tempDir.resolve("target.jpg"), "content");

		MoveBaseline baseline = new MoveBaseline(Files.size(target), fileHashService.sha256(target));

		Assertions.assertThatCode(() -> verifier.verify(source, target, baseline)).doesNotThrowAnyException();
	}

	@Test
	void verifyFailsWhenSourceStillExists() throws Exception {
		Path source = Files.writeString(tempDir.resolve("source.jpg"), "content");
		Path target = Files.writeString(tempDir.resolve("target.jpg"), "content");

		MoveBaseline baseline = new MoveBaseline(Files.size(target), fileHashService.sha256(target));

		Assertions.assertThatThrownBy(() -> verifier.verify(source, target, baseline))
				.isInstanceOf(MoveIntegrityException.class).hasMessageContaining("source still exists");
	}

	@Test
	void verifyFailsWhenTargetIsMissing() {
		Path source = tempDir.resolve("source.jpg");
		Path target = tempDir.resolve("target.jpg");

		MoveBaseline baseline = new MoveBaseline(1, "abc");

		Assertions.assertThatThrownBy(() -> verifier.verify(source, target, baseline))
				.isInstanceOf(MoveIntegrityException.class).hasMessageContaining("target does not exist");
	}

	@Test
	void verifyFailsWhenTargetSizeDiffers() throws Exception {
		Path source = tempDir.resolve("source.jpg");
		Path target = Files.writeString(tempDir.resolve("target.jpg"), "content");

		MoveBaseline baseline = new MoveBaseline(999, fileHashService.sha256(target));

		Assertions.assertThatThrownBy(() -> verifier.verify(source, target, baseline))
				.isInstanceOf(MoveIntegrityException.class).hasMessageContaining("differs from source size");
	}

	@Test
	void verifyFailsWhenTargetHashDiffers() throws Exception {
		Path source = tempDir.resolve("source.jpg");
		Path target = Files.writeString(tempDir.resolve("target.jpg"), "content");

		MoveBaseline baseline = new MoveBaseline(Files.size(target),
				"0000000000000000000000000000000000000000000000000000000000000000");

		Assertions.assertThatThrownBy(() -> verifier.verify(source, target, baseline))
				.isInstanceOf(MoveIntegrityException.class).hasMessageContaining("SHA-256 does not match");
	}
}