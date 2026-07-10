package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record OrganizationReconcileIssueResponse(@JsonIgnore Long catalogFileId, String path, String expectedPath,
		String actualPath, String message) {
}