package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Result of {@link OrganizationPreviewExportService#export}: the suggested ZIP
 * file name for the {@code Content-Disposition} header, paired with the
 * streaming body that writes it. Kept as a plain holder so the service itself
 * never touches {@code ResponseEntity}/{@code HttpHeaders} - those stay in
 * {@code OrganizationController}.
 */
public record OrganizationPreviewExport(String zipFileName, StreamingResponseBody body) {
}