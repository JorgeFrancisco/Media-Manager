package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.util.UuidV7;

public record OrganizationExecuteResponse(UUID executionId, String status, LocalDateTime startedAt,
		LocalDateTime finishedAt, String sourcePath, String targetPath, long plannedMoves, long moved, long skipped,
		long errors, boolean rejected, String message) {

	public OrganizationExecuteResponse(Long executionId, String status, LocalDateTime startedAt,
			LocalDateTime finishedAt, String sourcePath, String targetPath, long plannedMoves, long moved, long skipped,
			long errors, boolean rejected, String message) {
		this(executionId == null ? null : UuidV7.fromLegacy(executionId), status, startedAt, finishedAt, sourcePath,
				targetPath, plannedMoves, moved, skipped, errors, rejected, message);
	}
}