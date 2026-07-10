package br.com.jorgemelo.nimbusfilemanager.metadata.application.date;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.ResolvedMediaDate;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.model.MetadataResult;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.DateSource;

@Component
public class MediaDateResolver {

	private final CaptureDateValidator captureDateValidator;

	public MediaDateResolver(CaptureDateValidator captureDateValidator) {
		this.captureDateValidator = captureDateValidator;
	}

	public ResolvedMediaDate resolve(MetadataResult metadata) {
		LocalDateTime captureDate = captureDateValidator.validate(metadata.getCaptureDate());

		DateSource dateSource = captureDate == null ? null : metadata.getDateSource();

		return new ResolvedMediaDate(captureDate, dateSource);
	}
}