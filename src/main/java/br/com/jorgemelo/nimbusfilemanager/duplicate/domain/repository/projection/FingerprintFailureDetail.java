package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection;

/** One exhausted visual-fingerprint failure shown in the duplicates modal. */
public record FingerprintFailureDetail(String path, String error) {
}