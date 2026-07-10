package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.LocationFallbackMode;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.LocationSubdivision;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationLayout;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.LocationConfidence;

public record Defaults(boolean recursive, OrganizationLayout layout, boolean allowConflicts, boolean overwriteExisting,
		Integer size, Integer limit, String sourcePath, String targetPath, LocationSubdivision locationSubdivision,
		LocationConfidence locationMinConfidence, LocationFallbackMode locationFallback) {
}