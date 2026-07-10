package br.com.jorgemelo.nimbusfilemanager.metadata.application.facade;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.MetadataOptions;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.extractor.MetadataExtractor;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.model.MetadataResult;

@ExtendWith(MockitoExtension.class)
class MetadataFacadeTest {

	@TempDir
	Path tempDir;

	@Mock
	private MetadataExtractor metadataExtractor;

	@Test
	void extractShouldValidateFileAndDelegateToExtractor() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "content");

		MetadataOptions options = new MetadataOptions(true, false);

		MetadataResult expected = MetadataResult.builder().fileName("photo.jpg").build();

		when(metadataExtractor.extract(file, options)).thenReturn(expected);

		MetadataResult result = new MetadataFacade(metadataExtractor).extract(file, options);

		Assertions.assertThat(result).isSameAs(expected);
	}

	@Test
	void extractShouldRejectInvalidFileBeforeDelegating() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new MetadataFacade(metadataExtractor).extract(tempDir.resolve("missing.jpg"), null))
				.withMessageContaining("File does not exist");
	}
}