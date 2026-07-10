package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import java.util.UUID;

/**
 * Outcome of a Duplicados deletion (move to quarantine).
 * {@code configured=false} means the quarantine folder setting is still empty,
 * so nothing was attempted and the caller should point the user at
 * Configurações.
 */
public record DuplicateDeletionResult(boolean configured, int requested, int moved, int skipped, int errors,
		UUID executionId, String message) {
}