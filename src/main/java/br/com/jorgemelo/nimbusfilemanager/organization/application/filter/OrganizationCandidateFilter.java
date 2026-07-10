package br.com.jorgemelo.nimbusfilemanager.organization.application.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.organization.application.dto.OrganizationPreviewRequest;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.repository.projection.OrganizationCandidate;
import br.com.jorgemelo.nimbusfilemanager.settings.application.ScanExclusionService;
import br.com.jorgemelo.nimbusfilemanager.shared.util.ExtensionUtils;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;

@Component
public class OrganizationCandidateFilter {

	private final ScanExclusionService scanExclusionService;

	@Autowired
	public OrganizationCandidateFilter(ScanExclusionService scanExclusionService) {
		this.scanExclusionService = scanExclusionService;
	}

	public boolean matches(OrganizationCandidate candidate, OrganizationPreviewRequest request, String sourcePathText) {
		if (scanExclusionService.isExcluded(PathUtils.normalizePath(candidate.currentPath()))) {
			return false;
		}

		if (!request.recursiveValue()
				&& !PathUtils.normalize(candidate.currentFolder()).equalsIgnoreCase(sourcePathText)) {
			return false;
		}

		if (request.onlyCategories() != null && !request.onlyCategories().isEmpty()
				&& !request.onlyCategories().contains(candidate.category())) {
			return false;
		}

		if (request.onlySubcategories() != null && !request.onlySubcategories().isEmpty()
				&& !request.onlySubcategories().contains(candidate.subcategory())) {
			return false;
		}

		if (request.onlyExtensions() != null && !request.onlyExtensions().isEmpty()) {
			String extension = ExtensionUtils.normalize(candidate.extension());

			boolean matches = request.onlyExtensions().stream().map(ExtensionUtils::normalize)
					.anyMatch(extension::equals);

			if (!matches) {
				return false;
			}
		}

		return request.onlyFileTypes() == null || request.onlyFileTypes().isEmpty()
				|| request.onlyFileTypes().contains(candidate.fileType());
	}
}