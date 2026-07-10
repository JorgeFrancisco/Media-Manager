package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source;

import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.rdcw.RdcwUnavailableException;
import br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.usn.UsnCursorStore;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.UsnJournalProperties;

class WindowsFileChangeSourceProviderTest {

	private static final BooleanSupplier WINDOWS = () -> true;
	private static final BooleanSupplier NON_WINDOWS = () -> false;
	private static final Path ROOT = Path.of("C:/Media");

	private final UsnCursorStore cursorStore = mock(UsnCursorStore.class);
	private final UsnJournalProperties enabled = new UsnJournalProperties(true, 65_536);

	private WindowsFileChangeSourceProvider provider(UsnJournalProperties properties, BooleanSupplier windows,
			WindowsChangeSourceOpener opener) {
		return new WindowsFileChangeSourceProvider(properties, cursorStore, windows, opener);
	}

	@Test
	void usesTheNativeSourceOnWindowsWhenItOpens() {
		FileChangeSource source = mock(FileChangeSource.class);

		Optional<FileChangeSource> result = provider(enabled, WINDOWS, (_, _, _) -> source).open(ROOT);

		Assertions.assertThat(result).containsSame(source);
	}

	@Test
	void fallsBackToWatchServiceOnlyWhenTheRecursiveWatchCannotOpen() {
		WindowsChangeSourceOpener failing = (_, _, _) -> {
			throw new RdcwUnavailableException("no directory handle");
		};

		Assertions.assertThat(provider(enabled, WINDOWS, failing).open(ROOT)).isEmpty();
	}

	@Test
	void fallsBackWhenTheNativeLayerCannotLink() {
		WindowsChangeSourceOpener linkageError = (_, _, _) -> {
			throw new UnsatisfiedLinkError("kernel32!CreateFileW");
		};

		Assertions.assertThat(provider(enabled, WINDOWS, linkageError).open(ROOT)).isEmpty();
	}

	@Test
	void declinesOnNonWindowsPlatforms() {
		Assertions.assertThat(provider(enabled, NON_WINDOWS, unusableOpener()).open(Path.of("/library"))).isEmpty();
	}

	@Test
	void declinesWhenDisabled() {
		UsnJournalProperties disabled = new UsnJournalProperties(false, 65_536);

		Assertions.assertThat(provider(disabled, WINDOWS, unusableOpener()).open(ROOT)).isEmpty();
	}

	@Test
	void theProductionPlatformCheckMatchesTheRunningOs() {
		boolean expected = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

		Assertions.assertThat(WindowsFileChangeSourceProvider.isWindowsOs()).isEqualTo(expected);
	}

	private WindowsChangeSourceOpener unusableOpener() {
		return (_, _, _) -> {
			throw new AssertionError("opener must not be called");
		};
	}
}