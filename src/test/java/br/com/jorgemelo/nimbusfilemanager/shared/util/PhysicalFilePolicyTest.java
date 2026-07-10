package br.com.jorgemelo.nimbusfilemanager.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The physical-only policy: real files/directories are processable; symbolic
 * links (file and directory) and .lnk shortcuts are not. Junction/reparse
 * detection is Windows-specific and cannot be created portably, so it is not
 * asserted here.
 */
class PhysicalFilePolicyTest {

	@TempDir
	Path dir;

	@Test
	void processesRealFilesAndDirectories() throws IOException {
		Path file = Files.writeString(dir.resolve("photo.jpg"), "jpg");
		Path folder = Files.createDirectory(dir.resolve("album"));

		assertThat(PhysicalFilePolicy.isProcessable(file)).isTrue();
		assertThat(PhysicalFilePolicy.isProcessable(folder)).isTrue();
	}

	@Test
	void rejectsNullPath() {
		assertThat(PhysicalFilePolicy.isProcessable(null)).isFalse();
	}

	@Test
	void rejectsLnkShortcutsCaseInsensitively() throws IOException {
		Path lower = Files.writeString(dir.resolve("a.lnk"), "x");
		Path upper = Files.writeString(dir.resolve("B.LNK"), "x");

		assertThat(PhysicalFilePolicy.isProcessable(lower)).isFalse();
		assertThat(PhysicalFilePolicy.isProcessable(upper)).isFalse();
	}

	@Test
	void rejectsSymbolicLinkToFile() throws IOException {
		Path target = Files.writeString(dir.resolve("real.jpg"), "x");
		Path link = symlink(dir.resolve("link.jpg"), target);

		assertThat(PhysicalFilePolicy.isProcessable(link)).isFalse();
	}

	@Test
	void rejectsSymbolicLinkToDirectory() throws IOException {
		Path target = Files.createDirectory(dir.resolve("realdir"));
		Path link = symlink(dir.resolve("linkdir"), target);

		assertThat(PhysicalFilePolicy.isProcessable(link)).isFalse();
	}

	private Path symlink(Path link, Path target) {
		try {
			return Files.createSymbolicLink(link, target);
		} catch (IOException | UnsupportedOperationException | SecurityException exception) {
			return Assumptions.abort("Symbolic links are not supported in this environment: " + exception.getMessage());
		}
	}
}