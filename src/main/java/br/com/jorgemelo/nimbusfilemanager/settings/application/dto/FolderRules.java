package br.com.jorgemelo.nimbusfilemanager.settings.application.dto;

import java.util.List;

public record FolderRules(String raw, List<String> patterns, List<FolderMatcher> matchers) {

	public static final FolderRules EMPTY = new FolderRules(null, List.of(), List.of());
}