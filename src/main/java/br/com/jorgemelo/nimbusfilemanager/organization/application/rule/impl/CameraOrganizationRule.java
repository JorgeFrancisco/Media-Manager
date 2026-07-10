package br.com.jorgemelo.nimbusfilemanager.organization.application.rule.impl;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.family.CameraMediaFamily;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationRuleResult;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationRuleReason;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationRuleType;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;

@Component
public class CameraOrganizationRule extends AbstractOrganizationRule {

	@Override
	public int order() {
		return 90;
	}

	@Override
	public boolean supports(OrganizationCandidate candidate) {
		return matches(matchReason(candidate));
	}

	@Override
	public OrganizationRuleResult classify(OrganizationCandidate candidate) {
		return result(OrganizationRuleType.CAMERA, matchReason(candidate), candidate, MediaSubcategory.CAMERA);
	}

	private OrganizationRuleReason matchReason(OrganizationCandidate candidate) {
		if (candidate.subcategory() == MediaSubcategory.CAMERA
				|| candidate.subcategory() == MediaSubcategory.CELLPHONE) {
			return OrganizationRuleReason.DATABASE;
		}

		if (CameraMediaFamily.matchesPath(candidate.currentPath())) {
			return OrganizationRuleReason.FOLDER;
		}

		if (CameraMediaFamily.matchesName(candidate.fileName())) {
			return OrganizationRuleReason.FILE_NAME;
		}

		return null;
	}
}