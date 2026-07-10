package br.com.jorgemelo.nimbusfilemanager.inventory.application.batch;

import java.nio.file.Path;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionCancellationService;
import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionCancelledException;

/**
 * Pass-through processor: its only job is to abort the step as soon as the user
 * requests cancellation (checked once per item, same cadence the old
 * InventoryScanner used inside its scan callback), by throwing
 * {@link ExecutionCancelledException}. Actual persistence happens in
 * {@link InventoryItemWriter}, which is where the existing batched save/cache
 * logic lives.
 */
@Component
@StepScope
public class InventoryItemProcessor implements ItemProcessor<Path, Path> {

	private final ExecutionCancellationService executionCancellationService;
	private final Long executionId;

	public InventoryItemProcessor(ExecutionCancellationService executionCancellationService,
			@Value("#{jobParameters['executionId']}") Long executionId) {
		this.executionCancellationService = executionCancellationService;
		this.executionId = executionId;
	}

	@Override
	public Path process(Path item) {
		if (executionCancellationService.isCancelled(executionId)) {
			throw new ExecutionCancelledException("Inventory cancelled by user.");
		}

		return item;
	}
}