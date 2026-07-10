package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import java.util.List;
import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.util.UuidV7;

public record OrganizationUndoResponse(UUID executionId, String status, long total, long undone, long skipped,
		long errors, String message, List<OrganizationUndoItemResponse> items) {

	public OrganizationUndoResponse(Long executionId, String status, long total, long undone, long skipped, long errors,
			String message, List<OrganizationUndoItemResponse> items) {
		this(UuidV7.fromLegacy(executionId), status, total, undone, skipped, errors, message, items);
	}
}