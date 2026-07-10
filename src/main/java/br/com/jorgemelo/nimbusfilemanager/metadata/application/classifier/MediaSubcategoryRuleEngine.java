package br.com.jorgemelo.nimbusfilemanager.metadata.application.classifier;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;

@Service
public class MediaSubcategoryRuleEngine {

	private final List<MediaSubcategoryRule> rules;

	public MediaSubcategoryRuleEngine(List<MediaSubcategoryRule> rules) {
		this.rules = rules.stream().sorted(Comparator.comparing(MediaSubcategoryRule::name)).toList();
	}

	public MediaSubcategory resolve(String fileName, String path) {
		if (fileName == null) {
			return MediaSubcategory.UNKNOWN;
		}

		for (MediaSubcategoryRule rule : rules) {
			if (rule.supports(fileName, path)) {
				return rule.subcategory();
			}
		}

		return MediaSubcategory.UNKNOWN;
	}
}