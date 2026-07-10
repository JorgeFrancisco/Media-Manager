package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.util.UuidV7;

public record OrganizationUndoItemResponse(UUID movementId, UUID catalogFileId, String sourcePath, String targetPath,
		String status, String message) {

	public OrganizationUndoItemResponse(Long movementId, Long catalogFileId, String sourcePath, String targetPath,
			String status, String message) {
		this(UuidV7.fromLegacy(movementId), catalogFileId == null ? null : UuidV7.fromLegacy(catalogFileId), sourcePath,
				targetPath, status, message);
	}
}