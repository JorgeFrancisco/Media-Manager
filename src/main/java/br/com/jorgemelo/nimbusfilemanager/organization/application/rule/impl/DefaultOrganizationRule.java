package br.com.jorgemelo.nimbusfilemanager.organization.application.rule.impl;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationRuleResult;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationRuleReason;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationRuleType;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;

@Component
public class DefaultOrganizationRule extends AbstractOrganizationRule {

	@Override
	public int order() {
		return 999;
	}

	@Override
	public boolean supports(OrganizationCandidate candidate) {
		return true;
	}

	@Override
	public OrganizationRuleResult classify(OrganizationCandidate candidate) {
		return result(OrganizationRuleType.DEFAULT, OrganizationRuleReason.UNKNOWN, candidate, MediaSubcategory.OTHER);
	}
}