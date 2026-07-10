package br.com.jorgemelo.nimbusfilemanager.inventory.application.classifier;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.inventory.domain.enums.AnalysisErrorType;

@Component
public class AnalysisErrorClassifier {

	public AnalysisErrorType classify(Throwable throwable) {
		Throwable root = rootCause(throwable);

		String message = root.getMessage() == null ? "" : root.getMessage().toLowerCase();

		if (message.contains("verificação cíclica de redundância") || message.contains("cyclic redundancy check")
				|| message.contains("crc")) {
			return AnalysisErrorType.CRC_ERROR;
		}

		if (root instanceof AccessDeniedException || message.contains("access is denied")
				|| message.contains("acesso negado")) {
			return AnalysisErrorType.ACCESS_DENIED;
		}

		if (root instanceof NoSuchFileException || root instanceof FileNotFoundException
				|| message.contains("não é possível encontrar") || message.contains("cannot find")
				|| message.contains("file not found")) {
			return AnalysisErrorType.FILE_NOT_FOUND;
		}

		if (message.contains("hash")) {
			return AnalysisErrorType.HASH_ERROR;
		}

		if (message.contains("metadata") || message.contains("exif") || message.contains("mediainfo")) {
			return AnalysisErrorType.METADATA_ERROR;
		}

		return AnalysisErrorType.UNKNOWN;
	}

	private Throwable rootCause(Throwable throwable) {
		Throwable current = throwable;

		while (current.getCause() != null) {
			current = current.getCause();
		}

		return current;
	}
}