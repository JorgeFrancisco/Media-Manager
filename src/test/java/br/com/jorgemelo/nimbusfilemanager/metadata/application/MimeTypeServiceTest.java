package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

class MimeTypeServiceTest {

	@TempDir
	Path tempDir;

	private final MimeTypeService service = new MimeTypeService();

	@Test
	void shouldDetectCommonMimeTypesAndFileTypes() throws Exception {
		Path text = Files.writeString(tempDir.resolve("file.txt"), "hello");
		Path pdf = Files.write(tempDir.resolve("file.pdf"), "%PDF-1.4".getBytes());
		Path audio = Files.write(tempDir.resolve("file.mp3"), new byte[] { 1, 2, 3 });
		Path video = Files.write(tempDir.resolve("file.mp4"), new byte[] { 1, 2, 3 });
		Path archive = Files.write(tempDir.resolve("file.zip"), new byte[] { 1, 2, 3 });

		Assertions.assertThat(service.detect(text)).startsWith("text/");
		Assertions.assertThat(service.detectFileType(pdf)).isEqualTo(FileType.PDF);
		Assertions.assertThat(service.isText(text)).isTrue();
		Assertions.assertThat(service.isDocument(pdf)).isTrue();
		Assertions.assertThat(service.isAudio(audio)).isTrue();
		Assertions.assertThat(service.isVideo(video)).isTrue();
		Assertions.assertThat(service.isArchive(archive)).isTrue();
	}

	@Test
	void shouldFallbackToExtensionWhenMimeTypeIsGeneric() throws Exception {
		Path file = Files.write(tempDir.resolve("image.heic"), new byte[] { 1, 2, 3 });

		Assertions.assertThat(service.detectFileType(file)).isEqualTo(FileType.PHOTO);
	}

	@Test
	void shouldRejectInvalidPath() {
		assertThatIllegalArgumentException().isThrownBy(() -> service.detect(null))
				.withMessage("File path must not be null.");
		assertThatIllegalArgumentException().isThrownBy(() -> service.detect(tempDir.resolve("missing.txt")))
				.withMessageContaining("File does not exist");
		assertThatIllegalArgumentException().isThrownBy(() -> service.detect(tempDir))
				.withMessageContaining("Path is not a regular file");
	}

	@Test
	void shouldFallbackToDefaultMimeTypeWhenDetectorReturnsBlankOrThrows() throws Exception {
		Path file = Files.writeString(tempDir.resolve("file.bin"), "content");
		MimeTypeService nullMime = new MimeTypeService(_ -> null);
		MimeTypeService blank = new MimeTypeService(_ -> " ");
		MimeTypeService failure = new MimeTypeService(_ -> {
			throw new IOException("bad");
		});

		Assertions.assertThat(nullMime.detect(file)).isEqualTo("application/octet-stream");
		Assertions.assertThat(blank.detect(file)).isEqualTo("application/octet-stream");
		Assertions.assertThat(failure.detect(file)).isEqualTo("application/octet-stream");
	}

	@Test
	void shouldExposeAllFileTypePredicates() throws Exception {
		Path photo = Files.writeString(tempDir.resolve("photo.jpg"), "content");
		Path word = Files.writeString(tempDir.resolve("document.docx"), "content");
		Path excel = Files.writeString(tempDir.resolve("sheet.xlsx"), "content");
		Path powerPoint = Files.writeString(tempDir.resolve("deck.pptx"), "content");
		Path binary = Files.writeString(tempDir.resolve("file.unknown"), "content");
		MimeTypeService fallback = new MimeTypeService(_ -> "application/octet-stream");

		Assertions.assertThat(fallback.isImage(photo)).isTrue();
		Assertions.assertThat(fallback.isPdf(photo)).isFalse();
		Assertions.assertThat(fallback.isWord(word)).isTrue();
		Assertions.assertThat(fallback.isExcel(excel)).isTrue();
		Assertions.assertThat(fallback.isPowerPoint(powerPoint)).isTrue();
		Assertions.assertThat(fallback.isDocument(word)).isTrue();
		Assertions.assertThat(fallback.isArchive(binary)).isFalse();
	}
}