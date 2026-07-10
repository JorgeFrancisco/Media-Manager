package br.com.jorgemelo.nimbusfilemanager.settings.application;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.repository.CatalogFileRepository;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.WorkspaceManager;

@ExtendWith(MockitoExtension.class)
class LibraryCatalogCleanupServiceTest {

	@TempDir
	Path tempDir;

	@Mock
	private CatalogFileRepository catalogFileRepository;

	@Mock
	private WorkspaceManager workspaceManager;

	@Test
	void clearShouldDeleteCatalogRowsAndWipeThumbnailCacheContents() throws Exception {
		Path workspace = Files.createDirectories(tempDir.resolve("workspace"));
		Path cache = Files.createDirectories(workspace.resolve("cache").resolve("thumbnails"));
		Path thumb = Files.writeString(cache.resolve("a.webp"), "x");
		Path nestedDir = Files.createDirectories(cache.resolve("2024"));
		Path nestedThumb = Files.writeString(nestedDir.resolve("b.webp"), "y");

		when(catalogFileRepository.deleteWithinLibrary(anyString(), anyString())).thenReturn(42);
		when(workspaceManager.getWorkspacePath()).thenReturn(workspace);
		when(workspaceManager.resolve("cache", "thumbnails")).thenReturn(cache);

		int deleted = service().clear("D:/library");

		Assertions.assertThat(deleted).isEqualTo(42);
		Assertions.assertThat(Files.exists(thumb)).isFalse();
		Assertions.assertThat(Files.exists(nestedThumb)).isFalse();
		Assertions.assertThat(Files.exists(nestedDir)).isFalse();
		// The cache directory itself is preserved; only its contents are wiped.
		Assertions.assertThat(Files.exists(cache)).isTrue();
	}

	@Test
	void clearShouldSkipCacheWipeWhenCacheIsOutsideWorkspace() {
		Path workspace = tempDir.resolve("workspace");

		when(catalogFileRepository.deleteWithinLibrary(anyString(), anyString())).thenReturn(0);
		when(workspaceManager.getWorkspacePath()).thenReturn(workspace);
		when(workspaceManager.resolve("cache", "thumbnails")).thenReturn(tempDir.resolve("outside"));

		// Cache path is not inside the workspace, so the wipe is skipped without error.
		Assertions.assertThat(service().clear("D:/library")).isZero();
	}

	private LibraryCatalogCleanupService service() {
		return new LibraryCatalogCleanupService(catalogFileRepository, workspaceManager);
	}
}