package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection;

/** One exhausted photo-fingerprint failure shown in the duplicates modal. */
public record PhotoFingerprintFailureResponse(String path, String error) {
}