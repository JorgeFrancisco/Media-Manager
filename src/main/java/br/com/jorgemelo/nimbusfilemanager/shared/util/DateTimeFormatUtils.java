package br.com.jorgemelo.nimbusfilemanager.shared.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Human-friendly rendering of {@link LocalDateTime} for the UI:
 * {@code dd/MM/yyyy HH:mm:ss.SSS}, <b>always</b> with the three-digit
 * millisecond suffix (so a value without sub-second precision shows
 * {@code .000}) for a consistent column. Called from Thymeleaf via
 * {@code T(...DateTimeFormatUtils).human(value)} so a raw ISO string (e.g.
 * {@code 2021-03-25T20:40:50}) never reaches a screen. {@code null} renders as
 * an em dash.
 */
public final class DateTimeFormatUtils {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
	private static final String EMPTY = "—";

	private DateTimeFormatUtils() {
	}

	public static String human(LocalDateTime value) {
		return value == null ? EMPTY : value.format(FORMATTER);
	}
}