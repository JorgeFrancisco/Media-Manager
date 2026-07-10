package br.com.jorgemelo.nimbusfilemanager.inventory.application.batch;

import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionCancellationService;
import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionCancelledException;

@ExtendWith(MockitoExtension.class)
class InventoryItemProcessorTest {

	@Mock
	private ExecutionCancellationService executionCancellationService;

	@Test
	void shouldReturnItemUnchangedWhenNotCancelled() {
		Path file = Path.of("C:/media/photo.jpg");

		when(executionCancellationService.isCancelled(9L)).thenReturn(false);

		Path result = new InventoryItemProcessor(executionCancellationService, 9L).process(file);

		Assertions.assertThat(result).isSameAs(file);
	}

	@Test
	void shouldThrowExecutionCancelledExceptionWhenCancelled() {
		Path file = Path.of("C:/media/photo.jpg");

		when(executionCancellationService.isCancelled(9L)).thenReturn(true);

		InventoryItemProcessor processor = new InventoryItemProcessor(executionCancellationService, 9L);

		Assertions.assertThatThrownBy(() -> processor.process(file)).isInstanceOf(ExecutionCancelledException.class)
				.hasMessage("Inventory cancelled by user.");
	}
}