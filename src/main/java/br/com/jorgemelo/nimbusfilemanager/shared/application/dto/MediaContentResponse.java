package br.com.jorgemelo.nimbusfilemanager.shared.application.dto;

import java.nio.file.Path;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import br.com.jorgemelo.nimbusfilemanager.timeline.application.dto.ByteRange;
import jakarta.servlet.http.HttpServletResponse;

public record MediaContentResponse(Path file, long totalLength, ByteRange byteRange, String fileName,
		String contentType) {

	public void applyTo(HttpServletResponse response) {
		response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
		response.setContentType(contentType);
		response.setStatus(byteRange.partial() ? HttpStatus.PARTIAL_CONTENT.value() : HttpStatus.OK.value());
		response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(byteRange.length()));

		if (byteRange.partial()) {
			response.setHeader(HttpHeaders.CONTENT_RANGE,
					"bytes " + byteRange.start() + "-" + byteRange.end() + "/" + totalLength);
		}
	}
}