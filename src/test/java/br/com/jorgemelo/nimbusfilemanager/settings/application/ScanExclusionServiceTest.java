package br.com.jorgemelo.nimbusfilemanager.settings.application;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;

class ScanExclusionServiceTest {

	@Test
	void shouldUseDefaultsAndExcludeConfiguredExtensionAndFolder() {
		ScanExclusionService service = new ScanExclusionService(null);

		Assertions.assertThat(service.excludedExtensions()).contains("tmp", "crdownload");
		Assertions.assertThat(service.excludedFolders()).contains("target", ".git");
		Assertions.assertThat(service.isExcluded(Path.of("target", "file.jpg"))).isTrue();
		Assertions.assertThat(service.isExcluded(Path.of("photo.tmp"))).isTrue();
		Assertions.assertThat(service.isExcluded(Path.of("photo.jpg"))).isFalse();
	}

	@Test
	void shouldNormalizeSplitAndDeduplicateConfiguredValues() {
		AppSettingService settings = mock(AppSettingService.class);

		when(settings.stringValue(eq(SettingsConstants.SCAN_EXCLUDED_EXTENSIONS), anyString()))
				.thenReturn(" JPG, jpg; ,tmp ");
		when(settings.stringValue(eq(SettingsConstants.SCAN_EXCLUDED_FOLDERS), anyString()))
				.thenReturn("Cache;build\ncache");

		ScanExclusionService service = new ScanExclusionService(settings);

		Assertions.assertThat(service.excludedExtensions()).containsExactly("jpg", "tmp");
		Assertions.assertThat(service.excludedFolders()).containsExactly("Cache", "build", "cache");
	}

	@Test
	void rebuildsMemoizedRulesWhenTheSettingChanges() {
		AppSettingService settings = mock(AppSettingService.class);

		when(settings.stringValue(eq(SettingsConstants.SCAN_EXCLUDED_FOLDERS), anyString())).thenReturn("cache",
				"backup");

		ScanExclusionService service = new ScanExclusionService(settings);

		// First call parses "cache"; the second sees a different raw string and
		// rebuilds.
		Assertions.assertThat(service.excludedFolders()).containsExactly("cache");
		Assertions.assertThat(service.excludedFolders()).containsExactly("backup");
	}

	@Test
	void shouldMatchWildcardFolderPatternsWithinRoot() {
		AppSettingService settings = mock(AppSettingService.class);

		when(settings.stringValue(eq(SettingsConstants.SCAN_EXCLUDED_FOLDERS), anyString()))
				.thenReturn("cache-?, backup*");

		ScanExclusionService service = new ScanExclusionService(settings);

		Path root = Path.of("library");

		Assertions.assertThat(service.isExcludedFolder(root, root.resolve("cache-1/photo.jpg"))).isTrue();
		Assertions.assertThat(service.isExcludedFolder(root, root.resolve("backup-old/photo.jpg"))).isTrue();
		Assertions.assertThat(service.isExcludedFolder(root, root.resolve("camera/photo.jpg"))).isFalse();
	}

	@Test
	void shouldReturnFalseForNullAndEmptyConfiguration() {
		AppSettingService settings = mock(AppSettingService.class);

		when(settings.stringValue(eq(SettingsConstants.SCAN_EXCLUDED_EXTENSIONS), anyString())).thenReturn("");
		when(settings.stringValue(eq(SettingsConstants.SCAN_EXCLUDED_FOLDERS), anyString())).thenReturn(null);

		ScanExclusionService service = new ScanExclusionService(settings);

		Assertions.assertThat(service.isExcluded((Path) null)).isFalse();
		Assertions.assertThat(service.excludedExtensions()).isEmpty();
		Assertions.assertThat(service.excludedFolders()).isEmpty();
		Assertions.assertThat(service.normalizeExtensions(null)).isEmpty();
		Assertions.assertThat(service.normalizePatterns(null)).isEmpty();
	}

	@Test
	void shouldTreatConfiguredQuarantineFolderAsExcluded() {
		AppSettingService settings = mock(AppSettingService.class);

		Path quarantine = Path.of("D:", "SEPARAR", "QUARENTENA").toAbsolutePath().normalize();

		when(settings.stringValue(eq(SettingsConstants.TRASH_FOLDER), anyString())).thenReturn(quarantine.toString());

		ScanExclusionService service = new ScanExclusionService(settings);

		Assertions.assertThat(service.quarantineRoot()).isEqualTo(quarantine);
		Assertions.assertThat(service.isWithinQuarantine(quarantine)).isTrue();
		Assertions.assertThat(service.isWithinQuarantine(quarantine.resolve("exec-13").resolve("trashed.m4a")))
				.isTrue();
		Assertions.assertThat(service.isExcluded(quarantine.resolve("exec-13").resolve("trashed.m4a"))).isTrue();
		Assertions
				.assertThat(service
						.isWithinQuarantine(Path.of("D:", "SEPARAR", "fotos", "img.jpg").toAbsolutePath().normalize()))
				.isFalse();
	}

	@Test
	void shouldReportNoQuarantineWhenUnconfigured() {
		ScanExclusionService service = new ScanExclusionService(null);

		Assertions.assertThat(service.quarantineRoot()).isNull();
		Assertions.assertThat(service.isWithinQuarantine(Path.of("anywhere", "file.jpg"))).isFalse();
	}

	@Test
	void shouldNotApplyRootRelativePatternToPathOutsideRoot() {
		AppSettingService settings = mock(AppSettingService.class);

		when(settings.stringValue(eq(SettingsConstants.SCAN_EXCLUDED_FOLDERS), anyString())).thenReturn("cache");

		ScanExclusionService service = new ScanExclusionService(settings);

		Assertions.assertThat(service.isExcludedFolder(Path.of("library"), Path.of("other", "cache", "file.jpg")))
				.isTrue();
		Assertions.assertThat(service.isExcludedFolder(Path.of("library"), null)).isFalse();
	}
}