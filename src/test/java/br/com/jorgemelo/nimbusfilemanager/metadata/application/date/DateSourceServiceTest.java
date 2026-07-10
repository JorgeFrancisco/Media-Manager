package br.com.jorgemelo.nimbusfilemanager.metadata.application.date;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.FileSystemDates;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.filename.FileNameDateRuleEngine;

@ExtendWith(MockitoExtension.class)
class DateSourceServiceTest {

	@TempDir
	Path tempDir;

	@Mock
	private FolderLayoutDateResolver folderLayoutDateResolver;

	@Mock
	private FileNameDateRuleEngine fileNameDateRuleEngine;

	@Test
	void resolveShouldValidateFileSystemModifiedFileNameAndFolderDates() throws Exception {
		Path file = Files.writeString(tempDir.resolve("IMG_20240509.jpg"), "content");

		LocalDateTime expected = LocalDateTime.of(2024, Month.MAY, 9, 10, 30);

		when(fileNameDateRuleEngine.resolve("IMG_20240509.jpg")).thenReturn(expected);
		when(folderLayoutDateResolver.resolve(file)).thenReturn(expected);

		DateSourceService service = service();

		Assertions.assertThat(service.resolveFromFileSystem(file)).isNotNull();
		Assertions.assertThat(service.resolveModifiedAt(file)).isNotNull();
		Assertions.assertThat(service.resolveFromFileName(file)).isEqualTo(expected);
		Assertions.assertThat(service.resolveFromFolderLayout(file)).isEqualTo(expected);
	}

	@Test
	void resolveShouldDiscardDatesOutsideAcceptedRange() throws Exception {
		Path file = Files.writeString(tempDir.resolve("old.jpg"), "content");

		LocalDateTime old = LocalDateTime.of(1990, Month.JANUARY, 1, 0, 0);

		when(fileNameDateRuleEngine.resolve("old.jpg")).thenReturn(old);

		Assertions.assertThat(service().resolveFromFileName(file)).isNull();
	}

	@Test
	void resolveShouldWrapFileSystemReadFailures() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "content");

		DateSourceService service = service(new FileDateReader() {

			@Override
			public BasicFileAttributes readAttributes(Path file) throws IOException {
				throw new IOException("attributes failed");
			}

			@Override
			public FileTime getLastModifiedTime(Path file) {
				return FileTime.fromMillis(0);
			}
		});

		Assertions.assertThatThrownBy(() -> service.resolveFromFileSystem(file))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Could not read file dates");
	}

	@Test
	void resolveShouldFallbackToLastModifiedWhenCreationTimeIsMissing() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "content");

		FileTime modified = FileTime.fromMillis(1_700_000_000_000L);

		DateSourceService service = service(new FileDateReader() {

			@Override
			public BasicFileAttributes readAttributes(Path file) {
				return new BasicFileAttributes() {

					@Override
					public FileTime lastModifiedTime() {
						return modified;
					}

					@Override
					public FileTime lastAccessTime() {
						return modified;
					}

					@Override
					public FileTime creationTime() {
						return null;
					}

					@Override
					public boolean isRegularFile() {
						return true;
					}

					@Override
					public boolean isDirectory() {
						return false;
					}

					@Override
					public boolean isSymbolicLink() {
						return false;
					}

					@Override
					public boolean isOther() {
						return false;
					}

					@Override
					public long size() {
						return 1L;
					}

					@Override
					public Object fileKey() {
						return null;
					}
				};
			}

			@Override
			public FileTime getLastModifiedTime(Path file) {
				return modified;
			}
		});

		Assertions.assertThat(service.resolveFromFileSystem(file)).isNotNull();
	}

	@Test
	void resolveShouldWrapModifiedDateReadFailures() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "content");

		DateSourceService service = service(new FileDateReader() {

			@Override
			public BasicFileAttributes readAttributes(Path file) throws IOException {
				return Files.readAttributes(file, BasicFileAttributes.class);
			}

			@Override
			public FileTime getLastModifiedTime(Path file) throws IOException {
				throw new IOException("modified failed");
			}
		});

		Assertions.assertThatThrownBy(() -> service.resolveModifiedAt(file)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Could not read modified date");
	}

	@Test
	void resolveShouldReturnNullWhenFolderLayoutDateIsInvalid() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "content");
		LocalDateTime future = LocalDateTime.now().plusYears(2);

		when(folderLayoutDateResolver.resolve(file)).thenReturn(future);

		Assertions.assertThat(service().resolveFromFolderLayout(file)).isNull();
	}

	@Test
	void resolveFileSystemDatesShouldReadCreatedAndModifiedFromASingleAttributesCall() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "content");

		FileTime created = FileTime.fromMillis(1_600_000_000_000L);
		FileTime modified = FileTime.fromMillis(1_700_000_000_000L);

		AtomicInteger readAttributesCalls = new AtomicInteger();

		DateSourceService service = service(new FileDateReader() {

			@Override
			public BasicFileAttributes readAttributes(Path file) {
				readAttributesCalls.incrementAndGet();

				return new BasicFileAttributes() {

					@Override
					public FileTime lastModifiedTime() {
						return modified;
					}

					@Override
					public FileTime lastAccessTime() {
						return modified;
					}

					@Override
					public FileTime creationTime() {
						return created;
					}

					@Override
					public boolean isRegularFile() {
						return true;
					}

					@Override
					public boolean isDirectory() {
						return false;
					}

					@Override
					public boolean isSymbolicLink() {
						return false;
					}

					@Override
					public boolean isOther() {
						return false;
					}

					@Override
					public long size() {
						return 1L;
					}

					@Override
					public Object fileKey() {
						return null;
					}
				};
			}

			@Override
			public FileTime getLastModifiedTime(Path file) {
				throw new AssertionError("getLastModifiedTime should not be called by resolveFileSystemDates");
			}
		});

		FileSystemDates dates = service.resolveFileSystemDates(file);

		Assertions.assertThat(dates.createdAt())
				.isEqualTo(LocalDateTime.ofInstant(created.toInstant(), ZoneId.systemDefault()));
		Assertions.assertThat(dates.modifiedAt())
				.isEqualTo(LocalDateTime.ofInstant(modified.toInstant(), ZoneId.systemDefault()));
		Assertions.assertThat(readAttributesCalls).hasValue(1);
	}

	@Test
	void resolveFileSystemDatesShouldFallbackToLastModifiedWhenCreationTimeIsMissing() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "content");

		FileTime modified = FileTime.fromMillis(1_700_000_000_000L);

		DateSourceService service = service(new FileDateReader() {

			@Override
			public BasicFileAttributes readAttributes(Path file) {
				return new BasicFileAttributes() {

					@Override
					public FileTime lastModifiedTime() {
						return modified;
					}

					@Override
					public FileTime lastAccessTime() {
						return modified;
					}

					@Override
					public FileTime creationTime() {
						return null;
					}

					@Override
					public boolean isRegularFile() {
						return true;
					}

					@Override
					public boolean isDirectory() {
						return false;
					}

					@Override
					public boolean isSymbolicLink() {
						return false;
					}

					@Override
					public boolean isOther() {
						return false;
					}

					@Override
					public long size() {
						return 1L;
					}

					@Override
					public Object fileKey() {
						return null;
					}
				};
			}

			@Override
			public FileTime getLastModifiedTime(Path file) {
				return modified;
			}
		});

		FileSystemDates dates = service.resolveFileSystemDates(file);

		Assertions.assertThat(dates.createdAt())
				.isEqualTo(LocalDateTime.ofInstant(modified.toInstant(), ZoneId.systemDefault()));
	}

	@Test
	void resolveFileSystemDatesShouldWrapReadFailures() throws Exception {
		Path file = Files.writeString(tempDir.resolve("photo.jpg"), "content");

		DateSourceService service = service(new FileDateReader() {

			@Override
			public BasicFileAttributes readAttributes(Path file) throws IOException {
				throw new IOException("attributes failed");
			}

			@Override
			public FileTime getLastModifiedTime(Path file) {
				return FileTime.fromMillis(0);
			}
		});

		Assertions.assertThatThrownBy(() -> service.resolveFileSystemDates(file))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Could not read file dates");
	}

	private DateSourceService service() {
		return new DateSourceService(folderLayoutDateResolver, fileNameDateRuleEngine,
				new CaptureDateValidator(Clock.systemDefaultZone()));
	}

	private DateSourceService service(FileDateReader fileDateReader) {
		return new DateSourceService(folderLayoutDateResolver, fileNameDateRuleEngine,
				new CaptureDateValidator(Clock.systemDefaultZone()), fileDateReader);
	}
}