package br.com.jorgemelo.nimbusfilemanager.organization.application.rule;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationRuleResult;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;

public interface OrganizationRule {

	int order();

	boolean supports(OrganizationCandidate candidate);

	OrganizationRuleResult classify(OrganizationCandidate candidate);
}