package br.com.jorgemelo.nimbusfilemanager.inventory.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.jorgemelo.nimbusfilemanager.inventory.application.classifier.AnalysisErrorClassifier;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.enums.AnalysisErrorType;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.model.AnalysisError;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.AnalysisErrorRepository;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.Execution;

@ExtendWith(MockitoExtension.class)
class AnalysisErrorServiceTest {

	@Mock
	private AnalysisErrorRepository analysisErrorRepository;

	@Mock
	private AnalysisErrorClassifier analysisErrorClassifier;

	@Test
	void saveShouldClassifyAndPersistAnalysisError() {
		Execution execution = Execution.builder().id(1L).build();

		Exception exception = new IllegalArgumentException("bad file");

		when(analysisErrorClassifier.classify(exception)).thenReturn(AnalysisErrorType.METADATA_ERROR);

		service().save(Path.of("C:/input/photo.jpg"), exception, execution);

		ArgumentCaptor<AnalysisError> captor = ArgumentCaptor.forClass(AnalysisError.class);

		verify(analysisErrorRepository).save(captor.capture());

		Assertions.assertThat(captor.getValue().getExecution()).isSameAs(execution);
		Assertions.assertThat(captor.getValue().getErrorType()).isEqualTo(AnalysisErrorType.METADATA_ERROR);
		Assertions.assertThat(captor.getValue().getErrorMessage()).isEqualTo("bad file");
		Assertions.assertThat(captor.getValue().getPath()).endsWith("photo.jpg");

		verify(analysisErrorClassifier).classify(any());
	}

	private AnalysisErrorService service() {
		return new AnalysisErrorService(analysisErrorRepository, analysisErrorClassifier);
	}
}