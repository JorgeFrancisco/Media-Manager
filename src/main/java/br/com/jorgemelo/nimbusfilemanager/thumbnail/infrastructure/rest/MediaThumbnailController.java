package br.com.jorgemelo.nimbusfilemanager.thumbnail.infrastructure.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgemelo.nimbusfilemanager.thumbnail.application.PhotoThumbnailService;
import br.com.jorgemelo.nimbusfilemanager.thumbnail.application.VideoThumbnailService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/media")
public class MediaThumbnailController {

	private final PhotoThumbnailService thumbnailService;
	private final VideoThumbnailService videoThumbnailService;

	public MediaThumbnailController(PhotoThumbnailService thumbnailService,
			VideoThumbnailService videoThumbnailService) {
		this.thumbnailService = thumbnailService;
		this.videoThumbnailService = videoThumbnailService;
	}

	@GetMapping(value = "/{publicId}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
	@Operation(summary = "Returns a cached, orientation-corrected photo thumbnail")
	public ResponseEntity<FileSystemResource> thumbnail(@PathVariable UUID publicId,
			@RequestParam(name = "w", defaultValue = "320") int width) throws IOException {
		var thumbnail = thumbnailService.get(publicId, width);

		if (thumbnail.isEmpty()) {
			thumbnail = videoThumbnailService.get(publicId, width);
		}

		if (thumbnail.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		var value = thumbnail.get();

		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).eTag(value.etag())
				.cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
				.contentLength(Files.size(value.path())).body(new FileSystemResource(value.path()));
	}
}