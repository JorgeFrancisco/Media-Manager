package br.com.jorgemelo.nimbusfilemanager.organization.application.rule;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationRuleResult;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;

@Component
public class OrganizationRuleEngine {

	private final List<OrganizationRule> rules;

	public OrganizationRuleEngine(List<OrganizationRule> rules) {
		this.rules = rules.stream().sorted(Comparator.comparingInt(OrganizationRule::order)).toList();
	}

	public OrganizationRuleResult classify(OrganizationCandidate candidate) {
		return rules.stream().filter(rule -> rule.supports(candidate)).findFirst()
				.orElseThrow(() -> new IllegalStateException("No organization rule configured")).classify(candidate);
	}
}