package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.LocationFallbackMode;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.LocationSubdivision;
import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.OrganizationLayout;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.LocationConfidence;

/**
 * The whole organization form as a single cohesive value, so the web
 * controller's model-building and preference-saving helpers stay within the
 * parameter limit. {@code page}/{@code size} drive the preview pagination while
 * every other field is both rendered back onto the form and persisted as a user
 * preference.
 */
public record OrganizationForm(String sourcePath, String targetPath, boolean recursive, OrganizationLayout layout,
		Integer limit, boolean allowConflicts, boolean overwriteExisting, Integer page, Integer size,
		LocationSubdivision locationSubdivision, LocationConfidence locationMinConfidence,
		LocationFallbackMode locationFallback) {
}