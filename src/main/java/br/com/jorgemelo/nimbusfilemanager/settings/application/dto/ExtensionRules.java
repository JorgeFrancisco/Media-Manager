package br.com.jorgemelo.nimbusfilemanager.settings.application.dto;

import java.util.List;

public record ExtensionRules(String raw, List<String> extensions) {

	public static final ExtensionRules EMPTY = new ExtensionRules(null, List.of());
}