package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Guards the server-rendered message catalog used by browser-side UI updates.
 */
class JavaScriptI18nTest {

	@Test
	void layoutLoadsCatalogAndTranslatorBeforeApplicationScripts() throws Exception {
		String layout = read("src/main/resources/templates/fragments/layout.html");

		assertThat(layout).containsSubsequence("fragments/i18n-messages :: catalog", "@{/js/i18n.js}", "@{/js/app.js}",
				"@{/js/execution-status.js}");
	}

	@Test
	void translatorSupportsNumberedMessageParameters() throws Exception {
		String javascript = read("src/main/resources/static/js/i18n.js");

		assertThat(javascript).contains("window.NimbusFileManagerMessageCatalog", "window.NimbusFileManagerI18n",
				"replace(/\\{(\\d+)\\}/g");
	}

	@Test
	void dynamicScreensReadVisibleCopyFromTranslator() throws Exception {
		String duplicates = read("src/main/resources/static/js/pages/duplicates.js");
		String quarantine = read("src/main/resources/static/js/pages/quarantine.js");
		String timeline = read("src/main/resources/static/js/pages/timeline.js");
		String progress = read("src/main/resources/static/js/pages/execution-progress.js");

		assertThat(duplicates).contains("window.NimbusFileManagerI18n.t", "js.duplicates.movingProgress");
		assertThat(quarantine).contains("window.NimbusFileManagerI18n.t", "js.quarantine.purgeCompleted");
		assertThat(timeline).contains("new Intl.DateTimeFormat(i18n.locale", "js.timeline.loadingLibrary");
		assertThat(progress).contains("window.NimbusFileManagerI18n.t", "js.execution.cancelConfirm");
	}

	@Test
	void inlineConfirmationsReceiveLocalizedDataFromThymeleaf() throws Exception {
		String settings = read("src/main/resources/templates/app/settings.html");
		String duplicates = read("src/main/resources/templates/app/duplicates.html");

		assertThat(settings).contains("#{settings.geo.removeConfirm}", "#{settings.exec.cleanupAgeConfirm}",
				"#{settings.exec.cleanupKeepConfirm}");
		assertThat(duplicates).contains("th:data-confirm=\"#{duplicates.rebuild.confirm}\"");
		assertThat(settings + duplicates).doesNotContain("return confirm('");
	}

	@Test
	void quarantineSelectionSurvivesPageNavigation() throws Exception {
		String quarantine = read("src/main/resources/static/js/pages/quarantine.js");

		assertThat(quarantine).contains("nimbus-file-manager.quarantine.selection.v1",
				"window.localStorage.getItem(selectionStorageKey)", "window.localStorage.setItem(selectionStorageKey",
				"return Object.keys(selection)");
	}

	private String read(String path) throws Exception {
		return Files.readString(Path.of(path));
	}
}