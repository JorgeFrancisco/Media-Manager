package br.com.jorgemelo.nimbusfilemanager.settings.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.jorgemelo.nimbusfilemanager.settings.application.dto.FolderBrowserEntry;

class FolderBrowserServiceTest {

	@TempDir
	Path tempDir;

	private final FolderBrowserService service = new FolderBrowserService();

	@Test
	void shouldListOnlyDirectSubdirectories() throws Exception {
		Path album = Files.createDirectory(tempDir.resolve("Álbum"));

		Files.writeString(tempDir.resolve("foto.jpg"), "test");

		var result = service.browse(tempDir.toString());

		assertThat(result.currentPath()).isEqualTo(tempDir.toAbsolutePath().normalize().toString());
		assertThat(result.directories()).extracting(FolderBrowserEntry::path)
				.containsExactly(album.toAbsolutePath().normalize().toString());
	}

	@Test
	void shouldExposeFileSystemRootsAndRejectFiles() throws Exception {
		assertThat(service.browse(null).directories()).isNotEmpty();

		Path file = Files.writeString(tempDir.resolve("not-a-folder.txt"), "test");

		assertThatIllegalArgumentException().isThrownBy(() -> service.browse(file.toString()));
	}

	@Test
	void shouldHideHiddenAndSystemFolders() throws Exception {
		Files.createDirectory(tempDir.resolve("Fotos"));
		Files.createDirectory(tempDir.resolve(".config"));
		Files.createDirectory(tempDir.resolve("$RECYCLE.BIN"));

		var result = service.browse(tempDir.toString());

		assertThat(result.directories()).extracting(FolderBrowserEntry::name).containsExactly("Fotos");
	}

	@Test
	void formatRootNameAppendsVolumeLabelOnlyWhenHelpful() {
		assertThat(FolderBrowserService.formatRootName("G:\\", "Google Drive")).isEqualTo("G:\\ (Google Drive)");
		assertThat(FolderBrowserService.formatRootName("C:\\", "")).isEqualTo("C:\\");
		assertThat(FolderBrowserService.formatRootName("C:\\", null)).isEqualTo("C:\\");
		assertThat(FolderBrowserService.formatRootName("C:\\", "C:\\")).isEqualTo("C:\\");
		assertThat(FolderBrowserService.formatRootName("D:\\", "\\\\?\\Volume{abc}")).isEqualTo("D:\\");
	}
}