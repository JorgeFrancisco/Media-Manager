package br.com.jorgemelo.nimbusfilemanager.settings.application.dto;

import java.util.regex.Pattern;

public record FolderMatcher(String pattern, Pattern compiled) {

	public static FolderMatcher of(String pattern) {
		boolean wildcard = pattern.contains("*") || pattern.contains("?");

		return new FolderMatcher(pattern,
				wildcard ? Pattern.compile(globRegex(pattern), Pattern.CASE_INSENSITIVE) : null);
	}

	public boolean matches(String folderName) {
		return compiled == null ? folderName.equalsIgnoreCase(pattern) : compiled.matcher(folderName).matches();
	}

	private static String globRegex(String pattern) {
		StringBuilder regex = new StringBuilder();

		for (int index = 0; index < pattern.length(); index++) {
			char value = pattern.charAt(index);

			switch (value) {
			case '*' -> regex.append(".*");
			case '?' -> regex.append('.');
			default -> regex.append(Pattern.quote(String.valueOf(value)));
			}
		}

		return regex.toString();
	}
}