package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Guards that the Arquivos explorer reuses the shared media-cards component
 * without losing views.
 */
class FilesTemplateTest {

	@Test
	void explorerReusesTheSharedMediaCardComponentInBothViews() throws Exception {
		String html = Files.readString(Path.of("src/main/resources/templates/app/files.html"));

		// Both view modes stay: the Detalhes table cell and the icon grid both render
		// files through the
		// shared fragment, while directories are still rendered by the page itself.
		assertThat(html).contains("fragments/media-cards :: card(${item}, 320)")
				.contains("fragments/media-cards :: card(${item}, ${thumbWidth})")
				.contains("|media-grid ${explorer.viewMode()}|").contains("class=\"media-tile\"")
				.contains("class=\"media-cell\"")
				// Infinite scroll depends on these ids/fragments; they must survive the
				// migration.
				.contains("id=\"explorerRows\"").contains("id=\"explorerTiles\"").contains("th:fragment=\"rows\"")
				.contains("th:fragment=\"tiles\"")
				// The old bespoke tile classes are gone (fully migrated, not half-migrated).
				.doesNotContain("explorer-tile").doesNotContain("explorer-icons")
				// th:replace outranks th:unless on the same tag: the directory guard must sit
				// on an OUTER block
				// wrapping the fragment call, or folders render the fragment too and appear
				// duplicated. This
				// bare opening tag only exists when the guard and the replace are on separate
				// elements.
				.contains("<th:block th:unless=\"${item.directory()}\">");
	}

	@Test
	void explorerKeepsPerTypeIconColorsAfterAdoptingTheComponent() throws Exception {
		String css = Files.readString(Path.of("src/main/resources/static/css/pages/files.css"));

		// The shared component draws icons in one tone; these page-scoped rules
		// preserve the colors.
		assertThat(css).contains(".files-page .media-symbol.folder").contains(".files-page .media-symbol.pdf")
				.contains(".files-page .media-tile.missing")
				// The base table is vertical-align: top; the explorer centers its rows so the
				// Nome-cell
				// icon/thumbnail lines up with the other columns.
				.contains(".files-page .explorer-details td");
	}
}