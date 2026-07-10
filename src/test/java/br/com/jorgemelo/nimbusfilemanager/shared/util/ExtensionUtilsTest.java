package br.com.jorgemelo.nimbusfilemanager.shared.util;

import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtensionUtilsTest {

	@Test
	void normalizeShouldTrimLowercaseAndRemoveLeadingDot() {
		Assertions.assertThat(ExtensionUtils.normalize(" .JPG ")).isEqualTo("jpg");
		Assertions.assertThat(ExtensionUtils.normalize("MP4")).isEqualTo("mp4");
		Assertions.assertThat(ExtensionUtils.normalize(null)).isEmpty();
	}

	@Test
	void fromPathShouldReturnNormalizedExtension() {
		Assertions.assertThat(ExtensionUtils.fromPath(Path.of("C:/media/Photo.HEIC"))).isEqualTo("heic");
		Assertions.assertThat(ExtensionUtils.fromPath(Path.of("archive.tar.gz"))).isEqualTo("gz");
	}

	@Test
	void fromPathShouldReturnEmptyWhenPathHasNoExtension() {
		Assertions.assertThat(ExtensionUtils.fromPath(Path.of("README"))).isEmpty();
		Assertions.assertThat(ExtensionUtils.fromPath(Path.of("photo."))).isEmpty();
		Assertions.assertThat(ExtensionUtils.fromPath(null)).isEmpty();
	}
}