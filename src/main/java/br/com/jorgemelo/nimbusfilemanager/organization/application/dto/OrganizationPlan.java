package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import java.util.List;

import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationLayout;

public record OrganizationPlan(String sourcePath, String targetPath, OrganizationLayout layout, boolean execute,
		OrganizationSummary summary, List<OrganizationItem> items) {
}