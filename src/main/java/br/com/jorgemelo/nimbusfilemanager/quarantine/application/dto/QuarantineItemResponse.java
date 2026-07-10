package br.com.jorgemelo.nimbusfilemanager.quarantine.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One file currently held in the quarantine folder, as shown on the Quarentena
 * screen. Built from the {@code Movement} audit row that recorded the
 * soft-delete, enriched with live disk checks (whether the file is still
 * physically in quarantine, whether its original folder still exists, and
 * whether restoring it would collide with a file already at the original path)
 * and with the preview hints the shared media-card fragment needs
 * (thumbnail/open-file). Thumbnails and content are served by {@code publicId}
 * through {@code /api/media/...}, which now serves soft-deleted (quarantined)
 * files too, to logged-in users; {@code previewUrl} is the original-content URL
 * the lightbox opens.
 */
public record QuarantineItemResponse(UUID movementId, UUID executionId, UUID mediaPublicId, String fileName,
		String originalPath, String originalFolder, String quarantinePath, String quarantineFolder, Long sizeBytes,
		String sizeLabel, LocalDateTime quarantinedAt, boolean presentInQuarantine, boolean originFolderExists,
		boolean conflict, String fileType, String iconClass, String iconLabelKey, boolean image, boolean video,
		boolean pdf, boolean text, boolean audio, String previewUrl) {

	/**
	 * Alias so the shared media-card fragment can read every item type through the
	 * same {@code name()} accessor.
	 */
	public String name() {
		return fileName;
	}
}