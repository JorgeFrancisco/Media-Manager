package br.com.jorgemelo.nimbusfilemanager.organization.application.rule.impl;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.family.ScreenshotMediaFamily;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationRuleResult;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationRuleReason;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationRuleType;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;

@Component
public class ScreenshotOrganizationRule extends AbstractOrganizationRule {

	@Override
	public int order() {
		return 40;
	}

	@Override
	public boolean supports(OrganizationCandidate candidate) {
		return matches(matchReason(candidate));
	}

	@Override
	public OrganizationRuleResult classify(OrganizationCandidate candidate) {
		return result(OrganizationRuleType.SCREENSHOT, matchReason(candidate), candidate, MediaSubcategory.SCREENSHOT);
	}

	private OrganizationRuleReason matchReason(OrganizationCandidate candidate) {
		if (candidate.subcategory() == MediaSubcategory.SCREENSHOT) {
			return OrganizationRuleReason.DATABASE;
		}

		if (ScreenshotMediaFamily.matchesPath(candidate.currentPath())) {
			return OrganizationRuleReason.FOLDER;
		}

		if (ScreenshotMediaFamily.matchesName(candidate.fileName())) {
			return OrganizationRuleReason.FILE_NAME;
		}

		return null;
	}
}