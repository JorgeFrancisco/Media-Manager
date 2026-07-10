package br.com.jorgemelo.nimbusfilemanager.inventory.application;

import java.nio.file.Path;

import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.inventory.application.classifier.AnalysisErrorClassifier;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.model.AnalysisError;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.AnalysisErrorRepository;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.Execution;

@Service
public class AnalysisErrorService {

	private final AnalysisErrorRepository analysisErrorRepository;
	private final AnalysisErrorClassifier analysisErrorClassifier;

	public AnalysisErrorService(AnalysisErrorRepository analysisErrorRepository,
			AnalysisErrorClassifier analysisErrorClassifier) {
		this.analysisErrorRepository = analysisErrorRepository;
		this.analysisErrorClassifier = analysisErrorClassifier;
	}

	public void save(Path file, Exception exception, Execution execution) {
		AnalysisError error = AnalysisError.builder().execution(execution)
				.path(file.toAbsolutePath().normalize().toString())
				.errorType(analysisErrorClassifier.classify(exception)).errorMessage(exception.getMessage()).build();

		analysisErrorRepository.save(error);
	}
}