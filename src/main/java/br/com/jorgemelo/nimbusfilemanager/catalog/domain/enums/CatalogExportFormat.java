package br.com.jorgemelo.nimbusfilemanager.catalog.domain.enums;

import java.util.Locale;

/**
 * Output format of the catalog export. {@link #fromParam(String)} accepts the
 * value case-insensitively so {@code ?format=csv} and {@code ?format=CSV} both
 * work, throwing {@link IllegalArgumentException} for anything else.
 */
public enum CatalogExportFormat {

	CSV, JSON;

	public static CatalogExportFormat fromParam(String value) {
		if (value == null || value.isBlank()) {
			return CSV;
		}

		return CatalogExportFormat.valueOf(value.trim().toUpperCase(Locale.ROOT));
	}
}