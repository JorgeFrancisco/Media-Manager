package br.com.jorgemelo.nimbusfilemanager.organization.application.resolver;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationDate;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;

@Component
public class OrganizationDateResolver {

	private static final String MONTH_FORMAT = "%02d";
	private static final String YEAR_FORMAT = "%04d%02d";
	static final String SEM_DATA = "SEM_DATA";
	static final String DIA_SEM_DATA = "00";

	private static final Pattern SIX_DIGITS = Pattern.compile("\\d{6}");

	private static final Pattern ISO_YEAR_MONTH = Pattern.compile("\\d{4}-\\d{2}");

	public OrganizationDate resolve(OrganizationCandidate candidate) {
		LocalDateTime captureDate = candidate.captureDate();

		if (captureDate != null
				&& validDate(captureDate.getYear(), captureDate.getMonthValue(), captureDate.getDayOfMonth())) {
			return new OrganizationDate(String.format(YEAR_FORMAT, captureDate.getYear(), captureDate.getMonthValue()),
					String.format(MONTH_FORMAT, captureDate.getDayOfMonth()), false);
		}

		if (candidate.year() != null && candidate.month() != null && candidate.day() != null
				&& validDate(candidate.year(), candidate.month(), candidate.day())) {
			return new OrganizationDate(String.format(YEAR_FORMAT, candidate.year(), candidate.month()),
					String.format(MONTH_FORMAT, candidate.day()), false);
		}

		String normalizedYearMonth = normalizeYearMonth(candidate.yearMonth());

		if (normalizedYearMonth != null && candidate.day() != null && candidate.day() >= 1 && candidate.day() <= 31) {
			return new OrganizationDate(normalizedYearMonth, String.format(MONTH_FORMAT, candidate.day()), false);
		}

		return new OrganizationDate(SEM_DATA, DIA_SEM_DATA, true);
	}

	private String normalizeYearMonth(String yearMonth) {
		if (yearMonth == null || yearMonth.isBlank()) {
			return null;
		}

		String value = yearMonth.trim();

		if (SIX_DIGITS.matcher(value).matches()) {
			int year = Integer.parseInt(value.substring(0, 4));
			int month = Integer.parseInt(value.substring(4, 6));

			return validYearMonth(year, month) ? value : null;
		}

		if (ISO_YEAR_MONTH.matcher(value).matches()) {
			int year = Integer.parseInt(value.substring(0, 4));
			int month = Integer.parseInt(value.substring(5, 7));

			return validYearMonth(year, month) ? String.format(YEAR_FORMAT, year, month) : null;
		}

		return null;
	}

	private boolean validDate(int year, int month, int day) {
		return validYearMonth(year, month) && day >= 1 && day <= 31;
	}

	private boolean validYearMonth(int year, int month) {
		return year >= 1900 && year <= 2100 && month >= 1 && month <= 12;
	}
}