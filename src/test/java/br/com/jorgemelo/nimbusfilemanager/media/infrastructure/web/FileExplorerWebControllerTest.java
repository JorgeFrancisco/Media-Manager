package br.com.jorgemelo.nimbusfilemanager.media.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;

import br.com.jorgemelo.nimbusfilemanager.media.application.dto.FileExplorerView;
import br.com.jorgemelo.nimbusfilemanager.media.application.explorer.FileExplorerService;
import br.com.jorgemelo.nimbusfilemanager.preferences.application.UserPagePreferenceService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.ScanExclusionService;

/**
 * Behaviour of the file explorer controller: preference merge/save, sort
 * normalization (name vs other), infinite-scroll fragment selection with the
 * has-next header, sidebar toggle and the file-preview endpoint.
 */
class FileExplorerWebControllerTest {

	private final FileExplorerService fileExplorerService = mock(FileExplorerService.class);
	private final UserPagePreferenceService userPagePreferenceService = mock(UserPagePreferenceService.class);
	private final ScanExclusionService scanExclusionService = mock(ScanExclusionService.class);
	private final FileExplorerWebController controller = new FileExplorerWebController(fileExplorerService,
			userPagePreferenceService, scanExclusionService);

	private final Authentication auth = new TestingAuthenticationToken("bob", "x");

	@TempDir
	private Path tempDir;

	@Test
	void filesSavesRequestValuesAndBuildsModelWithNameSort() {
		when(userPagePreferenceService.find("bob", "files")).thenReturn(new HashMap<>());
		when(fileExplorerService.availableDrives()).thenReturn(List.of("C:"));

		FileExplorerView view = mock(FileExplorerView.class);

		when(fileExplorerService.browse("C:/x", "grid", 0, 50)).thenReturn(view);

		ExtendedModelMap model = new ExtendedModelMap();

		String result = controller.files("C:/x", "grid", 0, 50, "name", auth, model);

		Assertions.assertThat(result).isEqualTo("app/files");
		Assertions.assertThat(model).containsEntry("sort", "name");
		Assertions.assertThat(model.get("explorer")).isSameAs(view);
		Assertions.assertThat(model).containsKeys("sizes", "sortOptions", "drives");

		verify(userPagePreferenceService).save("bob", "files", "path", "C:/x");
		verify(userPagePreferenceService).save("bob", "files", "view", "grid");
		verify(userPagePreferenceService).save("bob", "files", "size", "50");
		verify(userPagePreferenceService).save("bob", "files", "sort", "name");
	}

	@Test
	void filesUsesSortedBrowseOverloadForNonNameSort() {
		when(userPagePreferenceService.find("bob", "files")).thenReturn(new HashMap<>());

		FileExplorerView view = mock(FileExplorerView.class);

		when(fileExplorerService.browse("C:/x", "grid", 0, 50, "date-newest")).thenReturn(view);

		ExtendedModelMap model = new ExtendedModelMap();

		String result = controller.files("C:/x", "grid", 0, 50, "date-newest", auth, model);

		Assertions.assertThat(result).isEqualTo("app/files");
		Assertions.assertThat(model).containsEntry("sort", "date-newest");
	}

	@Test
	void filesFallsBackToPreferencesWhenParamsAreAbsent() {
		Map<String, String> prefs = new HashMap<>(
				Map.of("path", "C:/pref", "view", "details", "size", "20", "sort", "name-desc"));

		when(userPagePreferenceService.find("bob", "files")).thenReturn(prefs);
		when(fileExplorerService.browse(eq("C:/pref"), eq("details"), any(), eq(20), eq("name-desc")))
				.thenReturn(mock(FileExplorerView.class));

		ExtendedModelMap model = new ExtendedModelMap();

		String result = controller.files(null, null, 0, null, null, auth, model);

		Assertions.assertThat(result).isEqualTo("app/files");
		Assertions.assertThat(model).containsEntry("sort", "name-desc");
	}

	@SuppressWarnings("unchecked")
	@Test
	void filesToleratesBlankAndInvalidSizePreference() {
		Map<String, String> blank = new HashMap<>();

		blank.put("size", "");

		Map<String, String> invalid = new HashMap<>();

		invalid.put("size", "abc");

		when(userPagePreferenceService.find("bob", "files")).thenReturn(blank, invalid);
		when(fileExplorerService.browse(any(), any(), anyInt(), any())).thenReturn(mock(FileExplorerView.class));

		Assertions.assertThat(controller.files(null, null, 0, null, "name", auth, new ExtendedModelMap()))
				.isEqualTo("app/files");
		Assertions.assertThat(controller.files(null, null, 0, null, "name", auth, new ExtendedModelMap()))
				.isEqualTo("app/files");
	}

	@Test
	void sixArgumentOverloadDelegatesWithNameSort() {
		when(userPagePreferenceService.find("bob", "files")).thenReturn(new HashMap<>());
		when(fileExplorerService.browse(any(), any(), anyInt(), any())).thenReturn(mock(FileExplorerView.class));

		Assertions.assertThat(controller.files("C:/x", "grid", 0, 50, auth, new ExtendedModelMap()))
				.isEqualTo("app/files");
	}

	@Test
	void filesUsesNullUsernameWhenUnauthenticated() {
		when(userPagePreferenceService.find(null, "files")).thenReturn(new HashMap<>());
		when(fileExplorerService.browse(any(), any(), anyInt(), any())).thenReturn(mock(FileExplorerView.class));

		Assertions.assertThat(controller.files(null, null, 0, null, "name", null, new ExtendedModelMap()))
				.isEqualTo("app/files");
	}

	@Test
	void itemsReturnsRowsFragmentAndHasNextHeader() {
		FileExplorerView view = mock(FileExplorerView.class);

		when(view.hasNext()).thenReturn(true);
		when(view.viewMode()).thenReturn("details");
		when(fileExplorerService.browse("C:/x", "grid", 1, 50)).thenReturn(view);

		MockHttpServletResponse response = new MockHttpServletResponse();

		ExtendedModelMap model = new ExtendedModelMap();

		String fragment = controller.items("C:/x", "grid", 1, 50, "name", response, model);

		Assertions.assertThat(fragment).isEqualTo("app/files :: rows");
		Assertions.assertThat(response.getHeader("X-Has-Next")).isEqualTo("true");
		Assertions.assertThat(model.get("explorer")).isSameAs(view);
	}

	@Test
	void itemsReturnsTilesFragmentForNonNameSort() {
		FileExplorerView view = mock(FileExplorerView.class);

		when(view.hasNext()).thenReturn(false);
		when(view.viewMode()).thenReturn("grid");
		when(fileExplorerService.browse("C:/x", "grid", 1, 50, "date-oldest")).thenReturn(view);

		MockHttpServletResponse response = new MockHttpServletResponse();

		String fragment = controller.items("C:/x", "grid", 1, 50, "date-oldest", response, new ExtendedModelMap());

		Assertions.assertThat(fragment).isEqualTo("app/files :: tiles");
		Assertions.assertThat(response.getHeader("X-Has-Next")).isEqualTo("false");
	}

	@Test
	void toggleSidebarFlipsStoredValueAndSaves() {
		Map<String, String> prefs = new HashMap<>();

		prefs.put("sidebar-collapsed", "false");

		when(userPagePreferenceService.find("bob", "layout")).thenReturn(prefs);

		Map<String, Boolean> result = controller.toggleSidebar(auth);

		Assertions.assertThat(result).containsEntry("collapsed", true);

		verify(userPagePreferenceService).save("bob", "layout", "sidebar-collapsed", "true");
	}

	@Test
	void previewReturnsFileWhenReadable(@TempDir Path dir) throws IOException {
		Path file = Files.writeString(dir.resolve("photo.txt"), "hello");

		when(scanExclusionService.isExcluded(any())).thenReturn(false);

		ResponseEntity<FileSystemResource> response = controller.preview(file.toString());

		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(response.getBody()).isNotNull();
	}

	@Test
	void previewReturnsNotFoundForExcludedFile(@TempDir Path dir) throws IOException {
		Path file = Files.writeString(dir.resolve("photo.txt"), "hello");

		when(scanExclusionService.isExcluded(any())).thenReturn(true);

		Assertions.assertThat(controller.preview(file.toString()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void previewReturnsNotFoundForMissingFile(@TempDir Path dir) throws IOException {
		when(scanExclusionService.isExcluded(any())).thenReturn(false);

		Assertions.assertThat(controller.preview(dir.resolve("missing.txt").toString()).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void previewDefaultsContentTypeForUnknownFile(@TempDir Path dir) throws IOException {
		Path file = Files.writeString(dir.resolve("blob"), "data");

		when(scanExclusionService.isExcluded(any())).thenReturn(false);

		Assertions.assertThat(controller.preview(file.toString()).getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void filesShouldBrowseExplorer() {
		FileExplorerView explorer = new FileExplorerView("C:/media", null, "large", true, true, false, 0, 0, 0, 0, 0,
				20, 0, 1, false, false, List.of());
		ExtendedModelMap model = new ExtendedModelMap();
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin@example.com", "password");

		when(userPagePreferenceService.find("admin@example.com", "files")).thenReturn(Map.of());
		when(fileExplorerService.browse("C:/media", "large", 0, 20)).thenReturn(explorer);

		String view = controller.files("C:/media", "large", 0, 20, authentication, model);

		Assertions.assertThat(view).isEqualTo("app/files");
		Assertions.assertThat(model).containsEntry("explorer", explorer);
		verify(userPagePreferenceService).save("admin@example.com", "files", "path", "C:/media");
		verify(userPagePreferenceService).save("admin@example.com", "files", "view", "large");
		verify(userPagePreferenceService).save("admin@example.com", "files", "size", "20");
	}

	@Test
	void filesShouldUseSavedPreferencesWhenOptionalParametersAreAbsent() {
		FileExplorerService explorerService = mock(FileExplorerService.class);
		UserPagePreferenceService preferences = mock(UserPagePreferenceService.class);
		ExtendedModelMap model = new ExtendedModelMap();
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin@example.com", "password");
		FileExplorerView explorer = new FileExplorerView("C:/media", null, "details", true, true, false, 0, 0, 0, 0, 0,
				50, 0, 1, false, false, List.of());

		when(preferences.find("admin@example.com", "files"))
				.thenReturn(Map.of("path", "C:/media with spaces", "view", "details", "size", "50"));
		when(explorerService.browse("C:/media with spaces", "details", 0, 50)).thenReturn(explorer);

		String view = new FileExplorerWebController(explorerService, preferences, mock(ScanExclusionService.class))
				.files(null, null, 0, null, authentication, model);

		Assertions.assertThat(view).isEqualTo("app/files");
		Assertions.assertThat(model).containsEntry("explorer", explorer);
		verify(explorerService).browse("C:/media with spaces", "details", 0, 50);
	}

	@Test
	void filesShouldIgnoreInvalidSavedSizeAndPreserveExplicitParameters() {
		FileExplorerService explorerService = mock(FileExplorerService.class);
		UserPagePreferenceService preferences = mock(UserPagePreferenceService.class);
		ExtendedModelMap model = new ExtendedModelMap();
		FileExplorerView explorer = new FileExplorerView("C:/media", null, "details", true, true, false, 0, 0, 0, 0, 0,
				50, 0, 1, false, false, List.of());

		when(preferences.find(null, "files")).thenReturn(Map.of("path", "C:/saved", "view", "large", "size", "bad"));
		when(explorerService.browse("C:/requested path", "small", -2, null)).thenReturn(explorer);

		new FileExplorerWebController(explorerService, preferences, mock(ScanExclusionService.class))
				.files("C:/requested path", "small", -2, null, null, model);

		verify(explorerService).browse("C:/requested path", "small", -2, null);
		verify(preferences).save(null, "files", "path", "C:/requested path");
		verify(preferences).save(null, "files", "view", "small");
	}

	@Test
	void previewShouldReturnNotFoundForMissingAndDirectoryPaths() throws Exception {
		Path missing = tempDir.resolve("missing file.jpg");
		Path directory = Files.createDirectory(tempDir.resolve("folder with spaces"));

		Assertions.assertThat(controller.preview(missing.toString()).getStatusCode().value()).isEqualTo(404);
		Assertions.assertThat(controller.preview(directory.toString()).getStatusCode().value()).isEqualTo(404);
	}

	@Test
	void previewShouldReturnInlineNoCacheResourceWithDetectedContentType() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "image");

		var response = controller.preview(file.toString());

		Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
		Assertions.assertThat(response.getHeaders().getFirst("Content-Disposition")).isEqualTo("inline");
		Assertions.assertThat(response.getHeaders().getCacheControl()).contains("no-cache");
		Assertions.assertThat(response.getHeaders().getContentLength()).isEqualTo(5);
		Assertions.assertThat(response.getBody()).isNotNull();
	}

	@Test
	void filesPreviewShouldHideExcludedFiles() throws Exception {
		Path file = Files.writeString(tempDir.resolve("secret.jpg"), "secret");

		when(scanExclusionService.isExcluded(file)).thenReturn(true);

		var response = controller.preview(file.toString());

		Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
	}
}