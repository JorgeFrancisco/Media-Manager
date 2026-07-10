package br.com.jorgemelo.nimbusfilemanager.settings.application.dto;

import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationLayout;

public record UpdatePreferencesForm(String filesView, Integer filesSize, OrganizationLayout organizationLayout,
		Boolean organizationRecursive, Boolean organizationAllowConflicts, Boolean organizationOverwriteExisting,
		Integer organizationSize, String theme) {
}