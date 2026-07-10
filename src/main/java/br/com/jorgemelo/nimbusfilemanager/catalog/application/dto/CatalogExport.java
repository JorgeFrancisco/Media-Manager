package br.com.jorgemelo.nimbusfilemanager.catalog.application.dto;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import br.com.jorgemelo.nimbusfilemanager.catalog.application.CatalogExportService;

/**
 * Result of {@link CatalogExportService#export}: the suggested download file
 * name and content type for the response headers, paired with the streaming
 * body that writes the catalog. A plain holder so the service never touches
 * {@code ResponseEntity}/{@code HttpHeaders} - those stay in the controller.
 */
public record CatalogExport(String fileName, String contentType, StreamingResponseBody body) {
}