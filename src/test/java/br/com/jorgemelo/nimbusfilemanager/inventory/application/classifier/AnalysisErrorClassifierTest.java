package br.com.jorgemelo.nimbusfilemanager.inventory.application.classifier;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.inventory.domain.enums.AnalysisErrorType;

class AnalysisErrorClassifierTest {

	private final AnalysisErrorClassifier classifier = new AnalysisErrorClassifier();

	@Test
	void shouldClassifyByRootExceptionType() {
		Assertions.assertThat(classifier.classify(new RuntimeException(new AccessDeniedException("photo.jpg"))))
				.isEqualTo(AnalysisErrorType.ACCESS_DENIED);
		Assertions.assertThat(classifier.classify(new RuntimeException(new NoSuchFileException("photo.jpg"))))
				.isEqualTo(AnalysisErrorType.FILE_NOT_FOUND);
		Assertions.assertThat(classifier.classify(new RuntimeException(new FileNotFoundException("photo.jpg"))))
				.isEqualTo(AnalysisErrorType.FILE_NOT_FOUND);
	}

	@Test
	void shouldClassifyByMessageContent() {
		Assertions.assertThat(classifier.classify(new RuntimeException("cyclic redundancy check")))
				.isEqualTo(AnalysisErrorType.CRC_ERROR);
		Assertions.assertThat(classifier.classify(new RuntimeException("hash calculation failed")))
				.isEqualTo(AnalysisErrorType.HASH_ERROR);
		Assertions.assertThat(classifier.classify(new RuntimeException("exif metadata failed")))
				.isEqualTo(AnalysisErrorType.METADATA_ERROR);
	}

	@Test
	void shouldFallbackToUnknown() {
		Assertions.assertThat(classifier.classify(new RuntimeException("unexpected")))
				.isEqualTo(AnalysisErrorType.UNKNOWN);
	}
}