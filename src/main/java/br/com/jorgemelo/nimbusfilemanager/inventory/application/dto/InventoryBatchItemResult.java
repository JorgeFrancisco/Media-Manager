package br.com.jorgemelo.nimbusfilemanager.inventory.application.dto;

import java.nio.file.Path;

/**
 * Per-file outcome of a {@link InventoryPersistenceService#saveOrCacheBatch}
 * call. Exactly one of {@code result} or {@code exception} is set: a non-null
 * exception means metadata extraction (or mapping) failed for that specific
 * file, without aborting the rest of the batch.
 */
public record InventoryBatchItemResult(Path file, InventoryPersistenceResult result, Exception exception) {

	public static InventoryBatchItemResult of(Path file, InventoryPersistenceResult result) {
		return new InventoryBatchItemResult(file, result, null);
	}

	public static InventoryBatchItemResult error(Path file, Exception exception) {
		return new InventoryBatchItemResult(file, null, exception);
	}

	public boolean failed() {
		return exception != null;
	}
}