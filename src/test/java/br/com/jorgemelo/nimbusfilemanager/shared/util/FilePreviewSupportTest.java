package br.com.jorgemelo.nimbusfilemanager.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.shared.util.enums.Kind;

class FilePreviewSupportTest {

	@Test
	void shouldClassifyEveryTypeSupportedByTheSharedViewer() {
		assertThat(FilePreviewSupport.kind("jpg")).isEqualTo(Kind.IMAGE);
		assertThat(FilePreviewSupport.kind("MP4")).isEqualTo(Kind.VIDEO);
		assertThat(FilePreviewSupport.kind("pdf")).isEqualTo(Kind.PDF);
		assertThat(FilePreviewSupport.kind("txt")).isEqualTo(Kind.TEXT);
		assertThat(FilePreviewSupport.kind("m4a")).isEqualTo(Kind.AUDIO);
		assertThat(FilePreviewSupport.kind("docx")).isEqualTo(Kind.NONE);
		assertThat(FilePreviewSupport.kind(null)).isEqualTo(Kind.NONE);
	}

	@Test
	void shouldPreferCatalogTypeAndFallBackToExtension() {
		assertThat(FilePreviewSupport.kind("PDF", "")).isEqualTo(Kind.PDF);
		assertThat(FilePreviewSupport.kind("OTHER", "pdf")).isEqualTo(Kind.PDF);
		assertThat(FilePreviewSupport.kind(null, "jpg")).isEqualTo(Kind.IMAGE);
	}
}