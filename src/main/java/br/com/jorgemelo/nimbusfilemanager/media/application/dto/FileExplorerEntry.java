package br.com.jorgemelo.nimbusfilemanager.media.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.util.FileTypeIcon;

public record FileExplorerEntry(

		String name,

		String path,

		boolean directory,

		boolean missing,

		boolean registered,

		String fileType,

		boolean image,

		boolean video,

		boolean pdf,

		boolean text,

		boolean audio,

		String previewUrl,

		Long sizeBytes,

		LocalDateTime modifiedAt,

		String modifiedAtLabel,

		Long catalogFileId,

		UUID mediaPublicId) {

	public String iconClass() {
		if (missing) {
			return "bi-question-circle missing";
		}

		if (directory) {
			return "bi-folder-fill folder";
		}

		return FileTypeIcon.iconClass(fileType);
	}

	public String iconLabelKey() {
		if (missing) {
			return "filetype.missing";
		}

		if (directory) {
			return "filetype.folder";
		}

		return FileTypeIcon.iconLabelKey(fileType);
	}
}