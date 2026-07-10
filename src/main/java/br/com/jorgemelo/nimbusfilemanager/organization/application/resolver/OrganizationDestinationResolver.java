package br.com.jorgemelo.nimbusfilemanager.organization.application.resolver;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationDate;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationDestination;
import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationRuleResult;
import br.com.jorgemelo.nimbusfilemanager.organization.application.rule.OrganizationRuleEngine;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;
import br.com.jorgemelo.nimbusfilemanager.shared.i18n.LocalizedComponent;

@Component
public class OrganizationDestinationResolver extends LocalizedComponent {

	private final OrganizationDateResolver dateResolver;
	private final OrganizationRuleEngine ruleEngine;
	private final OrganizationLayoutResolver layoutResolver;

	public OrganizationDestinationResolver(OrganizationDateResolver dateResolver, OrganizationRuleEngine ruleEngine,
			OrganizationLayoutResolver layoutResolver) {
		this.dateResolver = dateResolver;
		this.ruleEngine = ruleEngine;
		this.layoutResolver = layoutResolver;
	}

	public OrganizationDestination resolve(Path targetPath, String layout, OrganizationCandidate candidate) {
		return resolve(targetPath, layout, candidate, List.of());
	}

	public OrganizationDestination resolve(Path targetPath, String layout, OrganizationCandidate candidate,
			List<String> locationSegments) {
		OrganizationDate date = dateResolver.resolve(candidate);

		OrganizationRuleResult ruleResult = ruleEngine.classify(candidate);

		Path folder = locationSegments == null || locationSegments.isEmpty()
				? layoutResolver.resolveFolder(targetPath, layout, date.yearMonth(), date.day(),
						MediaSubcategory.folderNameOf(ruleResult.subcategory()),
						FileType.folderNameOf(ruleResult.fileType()))
				: layoutResolver.resolveFolder(targetPath, layout, date.yearMonth(), date.day(),
						MediaSubcategory.folderNameOf(ruleResult.subcategory()),
						FileType.folderNameOf(ruleResult.fileType()), locationSegments);

		Path file = folder.resolve(safeFileName(candidate.fileName())).normalize();

		if (!file.startsWith(folder.normalize())) {
			throw new IllegalArgumentException(
					message("backend.organization.destinationEscapedTarget", candidate.fileName()));
		}

		return new OrganizationDestination(folder, file, date, ruleResult);
	}

	private String safeFileName(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			throw new IllegalArgumentException(message("backend.organization.fileNameRequired"));
		}

		Path name = Path.of(fileName).getFileName();

		if (name == null || !name.toString().equals(fileName)) {
			throw new IllegalArgumentException(message("backend.organization.fileNameHasPathSegments", fileName));
		}

		return name.toString();
	}
}