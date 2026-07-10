package br.com.jorgemelo.nimbusfilemanager.thumbnail.infrastructure.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;

import br.com.jorgemelo.nimbusfilemanager.thumbnail.application.PhotoThumbnailService;
import br.com.jorgemelo.nimbusfilemanager.thumbnail.application.VideoThumbnailService;
import br.com.jorgemelo.nimbusfilemanager.thumbnail.application.dto.PhotoThumbnail;

/**
 * Thumbnail endpoint: serves the photo thumbnail, falls back to the video
 * thumbnail and returns 404 when neither exists.
 */
class MediaThumbnailControllerTest {

	private final PhotoThumbnailService photoThumbnailService = mock(PhotoThumbnailService.class);
	private final VideoThumbnailService videoThumbnailService = mock(VideoThumbnailService.class);
	private final MediaThumbnailController controller = new MediaThumbnailController(photoThumbnailService,
			videoThumbnailService);

	private final UUID id = UUID.fromString("01890000-0000-7000-8000-000000000001");

	@TempDir
	Path dir;

	@Test
	void servesPhotoThumbnailWhenAvailable() throws Exception {
		Path file = Files.writeString(dir.resolve("thumb.jpg"), "img");

		when(photoThumbnailService.get(id, 320)).thenReturn(Optional.of(new PhotoThumbnail(file, "etag-1")));

		var response = controller.thumbnail(id, 320);

		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(response.getBody()).isNotNull();
		Assertions.assertThat(response.getHeaders().getETag()).contains("etag-1");
	}

	@Test
	void fallsBackToVideoThumbnailWhenPhotoMissing() throws Exception {
		Path file = Files.writeString(dir.resolve("video-thumb.jpg"), "img");

		when(photoThumbnailService.get(id, 320)).thenReturn(Optional.empty());
		when(videoThumbnailService.get(id, 320)).thenReturn(Optional.of(new PhotoThumbnail(file, "etag-2")));

		var response = controller.thumbnail(id, 320);

		Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(response.getBody()).isNotNull();
	}

	@Test
	void returnsNotFoundWhenNoThumbnailExists() throws Exception {
		when(photoThumbnailService.get(id, 320)).thenReturn(Optional.empty());
		when(videoThumbnailService.get(id, 320)).thenReturn(Optional.empty());

		Assertions.assertThat(controller.thumbnail(id, 320).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}