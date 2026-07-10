package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import java.nio.file.Path;
import java.util.Locale;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;
import br.com.jorgemelo.nimbusfilemanager.shared.util.ExtensionUtils;

/**
 * Central content policy for files whose media extension disagrees with their
 * bytes.
 */
public final class MediaProcessingPolicy {

	private MediaProcessingPolicy() {
	}

	/**
	 * WhatsApp can store ZIP/Lottie packages with a {@code .webp} name. They remain
	 * cataloged for the Files screen, but must not enter media-specific pipelines.
	 */
	public static boolean isArchiveMasqueradingAsMedia(Path file, String detectedMimeType) {
		return file != null && isArchiveMasqueradingAsMedia(ExtensionUtils.fromPath(file), detectedMimeType);
	}

	public static boolean isArchiveMasqueradingAsMedia(String extension, String detectedMimeType) {
		String mimeType = detectedMimeType == null ? "" : detectedMimeType.trim().toLowerCase(Locale.ROOT);

		boolean zipContent = FileType.fromMimeType(mimeType).isArchive()
				|| "application/x-zip-compressed".equals(mimeType);

		return FileType.fromExtension(extension).isMedia() && zipContent;
	}
}