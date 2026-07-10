package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection;

public record DuplicateGroupRawResponse(String sha256, long files, long totalSizeBytes, long wastedSizeBytes) {
}