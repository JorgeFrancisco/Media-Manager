package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.PhysicalTreeWatcher;

class FileChangeSourceFactoryTest {

	@Test
	void usesTheProviderSourceWhenOneIsAvailable() throws IOException {
		FileChangeSource provided = mock(FileChangeSource.class);
		FileChangeSourceFactory factory = new FileChangeSourceFactory(_ -> Optional.of(provided));

		Assertions.assertThat(factory.create(Path.of("."), true)).isSameAs(provided);
	}

	@Test
	void fallsBackToTheWatchServiceSourceWhenTheProviderDeclines(@TempDir Path dir) throws IOException {
		FileChangeSourceFactory factory = new FileChangeSourceFactory(_ -> Optional.empty());

		FileChangeSource source = factory.create(dir, false);

		try {
			Assertions.assertThat(source).isInstanceOf(PhysicalTreeWatcher.class);
			Assertions.assertThat(source.root()).isEqualTo(dir);
		} finally {
			source.close();
		}
	}
}