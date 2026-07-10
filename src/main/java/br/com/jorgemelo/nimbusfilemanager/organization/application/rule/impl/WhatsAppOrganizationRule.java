package br.com.jorgemelo.nimbusfilemanager.organization.application.rule.impl;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.family.WhatsAppMediaFamily;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationRuleResult;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationRuleReason;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationRuleType;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;

@Component
public class WhatsAppOrganizationRule extends AbstractOrganizationRule {

	@Override
	public int order() {
		return 10;
	}

	@Override
	public boolean supports(OrganizationCandidate candidate) {
		return candidate.subcategory() == MediaSubcategory.WHATSAPP
				|| WhatsAppMediaFamily.matchesPath(candidate.currentPath())
				|| WhatsAppMediaFamily.matchesName(candidate.fileName());
	}

	@Override
	public OrganizationRuleResult classify(OrganizationCandidate candidate) {
		if (candidate.subcategory() == MediaSubcategory.WHATSAPP) {
			return result(OrganizationRuleType.WHATSAPP, OrganizationRuleReason.DATABASE, candidate,
					MediaSubcategory.WHATSAPP);
		}

		if (WhatsAppMediaFamily.matchesPath(candidate.currentPath())) {
			return result(OrganizationRuleType.WHATSAPP, OrganizationRuleReason.FOLDER, candidate,
					MediaSubcategory.WHATSAPP);
		}

		return result(OrganizationRuleType.WHATSAPP, OrganizationRuleReason.FILE_NAME, candidate,
				MediaSubcategory.WHATSAPP);
	}
}