package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import java.nio.file.Path;

public record OrganizationDestination(Path folder, Path file, OrganizationDate date,
		OrganizationRuleResult ruleResult) {
}