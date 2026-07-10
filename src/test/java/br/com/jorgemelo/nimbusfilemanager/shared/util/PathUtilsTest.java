package br.com.jorgemelo.nimbusfilemanager.shared.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PathUtilsTest {

	@Test
	void containsAnyFolderShouldMatchIgnoringCaseAndSeparators() {
		Assertions.assertThat(PathUtils.containsAnyFolder("C:/media/whatsapp/photo.jpg", "WHATSAPP")).isTrue();
		Assertions.assertThat(PathUtils.containsAnyFolder("C:\\media\\DJI\\photo.jpg", "DRONE", "DJI")).isTrue();
		Assertions.assertThat(PathUtils.containsAnyFolder("C:/media/camera/photo.jpg", " camera ")).isTrue();
		Assertions.assertThat(PathUtils.containsAnyFolder("C:/media/photos/photo.jpg", "CAMERA")).isFalse();
	}

	@Test
	void containsAnyFolderShouldRejectInvalidInput() {
		Assertions.assertThat(PathUtils.containsAnyFolder(null, "CAMERA")).isFalse();
		Assertions.assertThat(PathUtils.containsAnyFolder("   ", "CAMERA")).isFalse();
		Assertions.assertThat(PathUtils.containsAnyFolder("C:/media/CAMERA/photo.jpg")).isFalse();
		Assertions.assertThat(PathUtils.containsAnyFolder("C:/media/CAMERA/photo.jpg", (String[]) null)).isFalse();
		Assertions.assertThat(PathUtils.containsAnyFolder("C:/media/camera/photo.jpg", "drone", null)).isFalse();
	}

	@Test
	void descendantLikePatternAppendsSeparatorAndEscapesLikeSpecials() {
		Assertions.assertThat(PathUtils.descendantLikePattern("D:\\Media", "\\")).isEqualTo("D:\\\\Media\\\\%");
	}

	@Test
	void descendantLikePatternKeepsAnExistingTrailingSeparatorForADriveRoot() {
		Assertions.assertThat(PathUtils.descendantLikePattern("D:\\", "\\")).isEqualTo("D:\\\\%");
	}

	@Test
	void descendantLikePatternEscapesUnderscoreAndPercentInTheFolder() {
		Assertions.assertThat(PathUtils.descendantLikePattern("/mnt/a_b 50%", "/")).isEqualTo("/mnt/a\\_b 50\\%/%");
	}
}