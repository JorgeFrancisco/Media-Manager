package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

@Service
public class MimeTypeService {

	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	private final MimeDetector mimeDetector;

	@Autowired
	public MimeTypeService() {
		// A single Tika instance is reused across all calls: Tika's constructor parses
		// the
		// default MIME type configuration from the classpath, which is wasteful to redo
		// for
		// every single file scanned.
		this(new Tika()::detect);
	}

	MimeTypeService(MimeDetector mimeDetector) {
		this.mimeDetector = mimeDetector;
	}

	public String detect(Path file) {
		if (file == null) {
			throw new IllegalArgumentException("File path must not be null.");
		}

		if (!Files.exists(file)) {
			throw new IllegalArgumentException("File does not exist: " + file);
		}

		if (!Files.isRegularFile(file)) {
			throw new IllegalArgumentException("Path is not a regular file: " + file);
		}

		try {
			String mimeType = mimeDetector.detect(file);

			if (mimeType == null || mimeType.isBlank()) {
				return DEFAULT_MIME_TYPE;
			}

			return mimeType;
		} catch (IOException _) {
			return DEFAULT_MIME_TYPE;
		}
	}

	FileType detectFileType(Path file) {
		FileType typeByMime = FileType.fromMimeType(detect(file));

		if (!typeByMime.isOther()) {
			return typeByMime;
		}

		return FileType.fromPath(file);
	}

	boolean isImage(Path file) {
		return detectFileType(file).isPhoto();
	}

	public boolean isVideo(Path file) {
		return detectFileType(file).isVideo();
	}

	public boolean isAudio(Path file) {
		return detectFileType(file).isAudio();
	}

	public boolean isPdf(Path file) {
		return detectFileType(file).isPdf();
	}

	public boolean isWord(Path file) {
		return detectFileType(file).isWord();
	}

	public boolean isExcel(Path file) {
		return detectFileType(file).isExcel();
	}

	public boolean isPowerPoint(Path file) {
		return detectFileType(file).isPowerPoint();
	}

	public boolean isText(Path file) {
		return detectFileType(file).isText();
	}

	public boolean isDocument(Path file) {
		return detectFileType(file).isDocument();
	}

	public boolean isArchive(Path file) {
		return detectFileType(file).isArchive();
	}
}