package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;

public record DuplicateSummaryResponse(long groups, long duplicatedFiles, SizeResponse totalSize,
		SizeResponse wastedSize) {
}