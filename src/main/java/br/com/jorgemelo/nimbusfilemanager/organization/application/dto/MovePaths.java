package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import java.nio.file.Path;

/**
 * Source and destination of a single file move, grouped so the executor's audit
 * helpers stay within the parameter limit.
 */
public record MovePaths(Path source, Path target) {
}