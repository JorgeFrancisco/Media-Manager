package br.com.jorgemelo.nimbusfilemanager.metadata.application.filename;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FileNameDateRuleEngine {

	private final List<FileNameDateRule> rules;

	public FileNameDateRuleEngine(List<FileNameDateRule> rules) {
		this.rules = rules.stream().sorted(Comparator.comparing(FileNameDateRule::name)).toList();
	}

	public LocalDateTime resolve(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return null;
		}

		for (FileNameDateRule rule : rules) {
			if (!rule.supports(fileName)) {
				continue;
			}

			LocalDateTime date = rule.resolve(fileName);

			if (date != null) {
				return date;
			}
		}

		return null;
	}
}