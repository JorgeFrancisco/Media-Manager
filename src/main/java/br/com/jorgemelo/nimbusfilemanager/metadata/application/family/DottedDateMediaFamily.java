package br.com.jorgemelo.nimbusfilemanager.metadata.application.family;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.filename.rule.AbstractFileNameDateRule;

/**
 * Date-only family for the year-first dotted layout {@code YYYY.MM.DD} anywhere
 * in the name (common in scanned documents, e.g.
 * {@code "01. 2024.03.03. Limpeza.pdf"}).
 *
 * <p>
 * Takes the <b>first plausible</b> occurrence - month {@code 01-12}, day
 * {@code 01-31} validated in the regex - so a trailing year-last date is
 * neither matched nor confused with it:
 * {@code "2024.07.18. Edital 30.07.2024.pdf"} resolves to 2024-07-18 (the
 * {@code 30.07.2024} is {@code DD.MM.YYYY}, year-last, and never matches the
 * year-first pattern).
 *
 * <p>
 * The ambiguous {@code DD-MM-YYYY} / {@code DD.MM} formats are intentionally
 * out of scope (locale-dependent day/month order); those keep falling to the
 * filesystem fallback. Ordered after the app families and
 * {@code DASHED_DATE_TIME}, before the plain {@code GENERIC} digit scan.
 */
@Component
public class DottedDateMediaFamily extends AbstractFileNameDateRule {

	public DottedDateMediaFamily(Clock clock) {
		super(clock);
	}

	private static final String ORDER = "095_DOTTED_DATE";

	/** {@code year . (01-12) . (01-31)}, first occurrence via the lazy prefix. */
	private static final Pattern SUPPORTS = Pattern.compile(".*?(\\d{4})\\.(0[1-9]|1[0-2])\\.(0[1-9]|[12]\\d|3[01]).*");

	private static final Pattern EXTRACT = Pattern.compile(".*?(\\d{4})\\.(0[1-9]|1[0-2])\\.(0[1-9]|[12]\\d|3[01])",
			Pattern.CASE_INSENSITIVE);

	@Override
	public boolean supports(String fileName) {
		return fileName != null && SUPPORTS.matcher(fileName).matches();
	}

	@Override
	public LocalDateTime resolve(String fileName) {
		return parse(fileName, EXTRACT, "yyyyMMdd");
	}

	@Override
	public String name() {
		return ORDER;
	}
}