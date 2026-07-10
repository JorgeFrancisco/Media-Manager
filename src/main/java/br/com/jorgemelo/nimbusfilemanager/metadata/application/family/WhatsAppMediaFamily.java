package br.com.jorgemelo.nimbusfilemanager.metadata.application.family;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.classifier.MediaSubcategoryRule;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.filename.rule.AbstractFileNameDateRule;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;

/**
 * Single home for everything that defines the <b>WhatsApp</b> media family: how
 * to recognize it (by file name and by folder), which subcategory it maps to,
 * and how to read its capture date from the name.
 *
 * <p>
 * The same bean is picked up by both engines - as a
 * {@link MediaSubcategoryRule} by the classifier and as a
 * {@code FileNameDateRule} (via {@link AbstractFileNameDateRule}) by the
 * filename date engine - so adding or tuning the family means touching this one
 * class (name + folder + subcategory + date), not several scattered helpers.
 *
 * <p>
 * Precedence between families is the engines' ordering: {@link #name()}
 * {@code "010"} makes WhatsApp win over the camera/screenshot families for a
 * name that could match both, which replaces the former "not WhatsApp" guards.
 */
@Component
public class WhatsAppMediaFamily extends AbstractFileNameDateRule implements MediaSubcategoryRule {

	public WhatsAppMediaFamily(Clock clock) {
		super(clock);
	}

	/** Shared ordering key for both engines (lower first). */
	private static final String ORDER = "010_WHATSAPP";

	/**
	 * WhatsApp (Android) signature: the {@code -WAxxxx} / {@code _WAxxxx} suffix,
	 * independent of the prefix (IMG/VID/PTT/AUD/DOC/STK/NULL...). Anchored at the
	 * start and matched with {@code find()}, so no trailing {@code .*} is needed to
	 * consume the rest of the name - a single {@code .*} keeps the match linear
	 * (avoids the super-linear backtracking of two {@code .*} around a token).
	 */
	private static final Pattern NAME_SIGNATURE = Pattern.compile("^[a-z]+[-_].*[-_]WA\\d+", Pattern.CASE_INSENSITIVE);

	private static final String WHATSAPP_FOLDER = "WHATSAPP";

	private static final Pattern WA_DATE = Pattern.compile(".*?[-_](\\d{8})[-_]WA.*", Pattern.CASE_INSENSITIVE);

	// ---- Detection: single source of truth for the family ------------------

	public static boolean matchesName(String fileName) {
		return fileName != null && NAME_SIGNATURE.matcher(fileName).find();
	}

	public static boolean matchesPath(String path) {
		return PathUtils.containsAnyFolder(path, MediaSubcategory.WHATSAPP.folderName(), WHATSAPP_FOLDER);
	}

	// ---- Filename date rule facet ------------------------------------------

	@Override
	public boolean supports(String fileName) {
		return matchesName(fileName);
	}

	@Override
	public LocalDateTime resolve(String fileName) {
		return parse(fileName, WA_DATE, "yyyyMMdd");
	}

	// ---- Subcategory rule facet --------------------------------------------

	@Override
	public boolean supports(String fileName, String path) {
		return matchesName(fileName) || matchesPath(path);
	}

	@Override
	public MediaSubcategory subcategory() {
		return MediaSubcategory.WHATSAPP;
	}

	// ---- Shared ordering key (both engines) --------------------------------

	@Override
	public String name() {
		return ORDER;
	}
}