package br.com.jorgemelo.nimbusfilemanager.settings.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionCancellationService;
import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionQueryService;
import br.com.jorgemelo.nimbusfilemanager.execution.application.dto.ExecutionResponse;
import br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.InventoryWatchService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;

class LibrarySwitchServiceTest {

	@Test
	void shouldPauseCleanUpdateAndStartNewLibraryInOrder() {
		AppSettingService settings = mock(AppSettingService.class);
		InventoryWatchService watcher = mock(InventoryWatchService.class);
		ExecutionQueryService executions = mock(ExecutionQueryService.class);
		ExecutionCancellationService cancellations = mock(ExecutionCancellationService.class);
		LibraryCatalogCleanupService cleanup = mock(LibraryCatalogCleanupService.class);

		when(executions.active()).thenReturn(Optional.empty());

		LibrarySwitchService service = new LibrarySwitchService(settings, watcher, executions, cancellations, cleanup);

		service.switchLibrary("C:/old", "D:/new", "admin");

		var order = inOrder(watcher, cleanup, settings);

		order.verify(watcher).pause();
		order.verify(cleanup).clear("C:/old");
		order.verify(settings).update(SettingsConstants.WATCH_FOLDER, "D:/new", "admin");
		order.verify(watcher).reconfigureAndInventory();
	}

	@Test
	void shouldRequestCancellationUntilExecutionStops() {
		AppSettingService settings = mock(AppSettingService.class);
		InventoryWatchService watcher = mock(InventoryWatchService.class);
		ExecutionQueryService executions = mock(ExecutionQueryService.class);
		ExecutionCancellationService cancellations = mock(ExecutionCancellationService.class);
		LibraryCatalogCleanupService cleanup = mock(LibraryCatalogCleanupService.class);

		when(executions.active()).thenReturn(Optional.of(mock(ExecutionResponse.class))).thenReturn(Optional.empty());

		LibrarySwitchService service = new LibrarySwitchService(settings, watcher, executions, cancellations, cleanup);

		service.switchLibrary("C:/old", "D:/new", "admin");

		verify(cancellations).requestAllCancellations();
		verify(cleanup).clear("C:/old");
	}

	@Test
	void shouldRecoverMonitoringWhenSwitchFails() {
		AppSettingService settings = mock(AppSettingService.class);
		InventoryWatchService watcher = mock(InventoryWatchService.class);
		ExecutionQueryService executions = mock(ExecutionQueryService.class);
		ExecutionCancellationService cancellations = mock(ExecutionCancellationService.class);
		LibraryCatalogCleanupService cleanup = mock(LibraryCatalogCleanupService.class);

		when(executions.active()).thenReturn(Optional.empty());
		doThrow(new IllegalStateException("database unavailable")).when(cleanup).clear("C:/old");

		LibrarySwitchService service = new LibrarySwitchService(settings, watcher, executions, cancellations, cleanup);

		service.switchLibrary("C:/old", "D:/new", "admin");

		verify(watcher).reconfigureAndInventory();
	}

	@Test
	void validateNewFolderRejectsBlankOrMissingAndAcceptsExistingDirectory(@TempDir Path existing) {
		LibrarySwitchService service = new LibrarySwitchService(mock(AppSettingService.class),
				mock(InventoryWatchService.class), mock(ExecutionQueryService.class),
				mock(ExecutionCancellationService.class), mock(LibraryCatalogCleanupService.class));

		assertThatThrownBy(() -> service.validateNewFolder("   ")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("não existe");

		String missingFolder = existing.resolve("gone-" + System.nanoTime()).toString();

		assertThatThrownBy(() -> service.validateNewFolder(missingFolder)).isInstanceOf(IllegalArgumentException.class);
		assertThatCode(() -> service.validateNewFolder(existing.toString())).doesNotThrowAnyException();
	}

	@Test
	void switchLibraryFromUnconfiguredLibrarySkipsCatalogCleanup() {
		AppSettingService settings = mock(AppSettingService.class);
		InventoryWatchService watcher = mock(InventoryWatchService.class);
		ExecutionQueryService executions = mock(ExecutionQueryService.class);
		ExecutionCancellationService cancellations = mock(ExecutionCancellationService.class);
		LibraryCatalogCleanupService cleanup = mock(LibraryCatalogCleanupService.class);

		when(executions.active()).thenReturn(Optional.empty());

		LibrarySwitchService service = new LibrarySwitchService(settings, watcher, executions, cancellations, cleanup);

		service.switchLibrary("   ", "D:/new", "admin");

		verify(cleanup, never()).clear(anyString());
		verify(settings).update(SettingsConstants.WATCH_FOLDER, "D:/new", "admin");
		verify(watcher).reconfigureAndInventory();
	}

	@Test
	void switchLibraryPreservesInterruptFlagAndRecoversWhenInterrupted() {
		AppSettingService settings = mock(AppSettingService.class);
		InventoryWatchService watcher = mock(InventoryWatchService.class);
		ExecutionQueryService executions = mock(ExecutionQueryService.class);
		ExecutionCancellationService cancellations = mock(ExecutionCancellationService.class);
		LibraryCatalogCleanupService cleanup = mock(LibraryCatalogCleanupService.class);

		when(executions.active()).thenReturn(Optional.of(mock(ExecutionResponse.class)));
		// Interrupt the worker while it waits for the running execution to cancel; the
		// next sleep then throws InterruptedException.
		doAnswer(_ -> {
			Thread.currentThread().interrupt();
			return null;
		}).when(cancellations).requestAllCancellations();

		LibrarySwitchService service = new LibrarySwitchService(settings, watcher, executions, cancellations, cleanup);

		service.switchLibrary("C:/old", "D:/new", "admin");

		// Flag is re-raised for the caller, the folder was never swapped, and
		// monitoring is restored.
		assertThat(Thread.interrupted()).isTrue();

		verify(settings, never()).update(anyString(), anyString(), anyString());
		verify(watcher).reconfigureAndInventory();
	}

	@Test
	void switchLibrarySwallowsFailureWhenRecoveryAlsoFails() {
		AppSettingService settings = mock(AppSettingService.class);
		InventoryWatchService watcher = mock(InventoryWatchService.class);
		ExecutionQueryService executions = mock(ExecutionQueryService.class);
		ExecutionCancellationService cancellations = mock(ExecutionCancellationService.class);
		LibraryCatalogCleanupService cleanup = mock(LibraryCatalogCleanupService.class);

		when(executions.active()).thenReturn(Optional.empty());
		doThrow(new IllegalStateException("catalog offline")).when(cleanup).clear("C:/old");
		doThrow(new IllegalStateException("watcher offline")).when(watcher).reconfigureAndInventory();

		LibrarySwitchService service = new LibrarySwitchService(settings, watcher, executions, cancellations, cleanup);

		assertThatCode(() -> service.switchLibrary("C:/old", "D:/new", "admin")).doesNotThrowAnyException();

		verify(watcher).reconfigureAndInventory();
	}
}