package br.com.jorgemelo.nimbusfilemanager.metadata.application.date;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.RefinedDate;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.DateSource;

/**
 * Refines a name-derived capture date (day-precision, i.e. midnight) by
 * adopting a filesystem timestamp of the <b>same day</b> when one corroborates
 * it: this both gains the real time-of-day and validates that the timestamp
 * isn't a stray copy/sync date (if no filesystem date matches the day, none is
 * trusted).
 *
 * <p>
 * Only {@link DateSource#FILE_NAME} at midnight is refined - EXIF/MEDIA_INFO
 * are already the true instant, and the {@code FILE_CREATED_AT} fallback has no
 * trustworthy day to corroborate against, so it stays low-confidence. On
 * success the source becomes {@link DateSource#FILE_NAME_CONFIRMED} (high
 * trust). Prefers {@code modified} over {@code created}: on a copy the OS
 * usually sets {@code created} to the copy time while preserving
 * {@code modified} as the original mtime.
 */
@Component
public class CaptureDateRefiner {

	public RefinedDate refine(LocalDateTime captureDate, DateSource source, LocalDateTime createdAt,
			LocalDateTime modifiedAt) {
		RefinedDate unchanged = new RefinedDate(captureDate, source);

		if (source != DateSource.FILE_NAME || captureDate == null) {
			return unchanged;
		}

		if (!captureDate.toLocalTime().equals(LocalTime.MIDNIGHT)) {
			return unchanged; // the name already carries a time; nothing to refine
		}

		LocalDateTime corroborating = firstMatchingDay(captureDate, modifiedAt, createdAt);

		return corroborating == null ? unchanged : new RefinedDate(corroborating, DateSource.FILE_NAME_CONFIRMED);
	}

	/**
	 * First timestamp (in preference order) whose date equals the name's day, or
	 * null.
	 */
	private LocalDateTime firstMatchingDay(LocalDateTime nameDate, LocalDateTime... candidates) {
		for (LocalDateTime candidate : candidates) {
			if (candidate != null && candidate.toLocalDate().equals(nameDate.toLocalDate())) {
				return candidate;
			}
		}

		return null;
	}
}