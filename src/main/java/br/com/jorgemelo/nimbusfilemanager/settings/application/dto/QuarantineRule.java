package br.com.jorgemelo.nimbusfilemanager.settings.application.dto;

import java.nio.file.Path;

public record QuarantineRule(String raw, Path root) {

	public static final QuarantineRule EMPTY = new QuarantineRule(null, null);
}