package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;

public record DuplicateGroupResponse(String sha256, long files, SizeResponse totalSize, SizeResponse wastedSize) {
}