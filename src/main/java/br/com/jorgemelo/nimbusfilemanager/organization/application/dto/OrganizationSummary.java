package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

public record OrganizationSummary(long totalFiles, long filesWithDate, long filesWithoutDate, long alreadyOrganized,
		long plannedMoves, long totalSizeBytes, long conflicts, long targetAlreadyExists, long duplicateTargets) {
}