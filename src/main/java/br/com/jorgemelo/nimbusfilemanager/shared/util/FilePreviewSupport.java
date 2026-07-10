package br.com.jorgemelo.nimbusfilemanager.shared.util;

import java.util.Locale;
import java.util.Set;

import br.com.jorgemelo.nimbusfilemanager.shared.util.enums.Kind;

/**
 * Single source of truth for the file types supported by the shared media
 * viewer used by Arquivos, Duplicados and Timeline.
 */
public final class FilePreviewSupport {

	private static final String PDF = "PDF";

	private static final Set<String> IMAGES = Set.of("PHOTO", "JPG", "JPEG", "PNG", "GIF", "WEBP", "BMP");
	private static final Set<String> VIDEOS = Set.of("VIDEO", "MP4", "MOV", "M4V", "WEBM", "MKV", "AVI");
	private static final Set<String> TEXT = Set.of("TEXT", "TXT");
	private static final Set<String> AUDIO = Set.of("AUDIO", "MP3", "WAV", "OGG", "M4A", "AAC");

	private FilePreviewSupport() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	public static Kind kind(String extension) {
		String value = extension == null ? "" : extension.trim().toUpperCase(Locale.ROOT);

		if (IMAGES.contains(value)) {
			return Kind.IMAGE;
		}

		if (VIDEOS.contains(value)) {
			return Kind.VIDEO;
		}

		if (PDF.equals(value)) {
			return Kind.PDF;
		}

		if (TEXT.contains(value)) {
			return Kind.TEXT;
		}

		if (AUDIO.contains(value)) {
			return Kind.AUDIO;
		}

		return Kind.NONE;
	}

	/**
	 * Uses the type detected during inventory first and falls back to the file name
	 * extension. This also supports files whose extension is missing or misleading.
	 */
	public static Kind kind(String fileType, String extension) {
		Kind catalogKind = kind(fileType);

		return catalogKind == Kind.NONE ? kind(extension) : catalogKind;
	}
}