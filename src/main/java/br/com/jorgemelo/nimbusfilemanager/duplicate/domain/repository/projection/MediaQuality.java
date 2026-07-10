package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.projection;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.DateSource;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;

/**
 * Per-file quality/provenance signals used by the Duplicados screen to decide
 * (via {@code DuplicateKeepPolicy}) which copy to keep and to show a
 * "Resolução" column: display resolution, the system's best date and its
 * reliability ({@code dateSource}), the subcategory (WhatsApp/edited markers)
 * and whether it carries camera EXIF (manufacturer/model). Looked up in bulk by
 * public id so nothing threads these through the duplicate DTOs. Fields may be
 * null for files without media/EXIF info.
 */
public record MediaQuality(UUID publicId, Integer width, Integer height, LocalDateTime captureDate, boolean hasExif,
		MediaSubcategory subcategory, DateSource dateSource, boolean hasCameraExif) {
}