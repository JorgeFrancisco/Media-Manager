package br.com.jorgemelo.nimbusfilemanager.metadata.application.dto;

public record MetadataRebuildResponse(String sourcePath, boolean dryRun, int candidates, int rebuilt,
		int skippedMissing, int skippedWithoutLocation, int skippedUnsupportedType, int errors) {
}