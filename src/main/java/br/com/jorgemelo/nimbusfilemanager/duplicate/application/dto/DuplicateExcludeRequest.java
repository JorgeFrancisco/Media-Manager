package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import java.util.UUID;

/**
 * Body of the "hide from duplicate comparison" actions: {@code publicId} is set
 * when excluding a single file, {@code folder} when excluding a whole folder.
 */
public record DuplicateExcludeRequest(UUID publicId, String folder) {
}