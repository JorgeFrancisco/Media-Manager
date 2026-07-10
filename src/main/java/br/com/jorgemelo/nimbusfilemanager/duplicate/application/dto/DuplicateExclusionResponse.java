package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

/**
 * Result of a "hide from duplicate comparison" action: {@code created} is true
 * when a new exclusion was stored, false when it was already excluded.
 */
public record DuplicateExclusionResponse(boolean created) {
}