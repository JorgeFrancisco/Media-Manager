package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.CatalogFile;

public record MissingEntry(CatalogFile catalogFile, String missingPath) {
}