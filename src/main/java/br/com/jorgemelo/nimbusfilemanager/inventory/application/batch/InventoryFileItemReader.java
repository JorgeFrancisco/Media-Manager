package br.com.jorgemelo.nimbusfilemanager.inventory.application.batch;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.execution.application.OperationLock;
import br.com.jorgemelo.nimbusfilemanager.execution.application.OperationLockException;
import br.com.jorgemelo.nimbusfilemanager.execution.application.OperationLockService;
import br.com.jorgemelo.nimbusfilemanager.inventory.application.dto.ScanOptions;
import br.com.jorgemelo.nimbusfilemanager.inventory.application.scanner.FileScanner;
import br.com.jorgemelo.nimbusfilemanager.settings.application.ScanExclusionService;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionType;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Chunk-oriented Spring Batch {@link ItemStreamReader} that lazily walks the
 * configured folder, emitting one {@link Path} at a time to the step's
 * processor/writer. Backed by {@link FileScanner#stream}, the same filtering
 * pipeline used by the old callback-driven scan.
 *
 * <p>
 * The operation lock that used to wrap the whole synchronous scan (see the
 * removed InventoryScanner/InventoryAsyncRunner) is acquired in {@link #open}
 * and released in {@link #close}, so it still covers the entire read phase of
 * the step for this execution.
 */
@Slf4j
@Component
@StepScope
public class InventoryFileItemReader implements ItemStreamReader<Path> {

	private final FileScanner fileScanner;
	private final OperationLockService operationLockService;

	private final Path sourcePath;
	private final ScanOptions scanOptions;

	private Stream<Path> stream;
	private Iterator<Path> iterator;
	private OperationLock lock;

	public InventoryFileItemReader(FileScanner fileScanner, ScanExclusionService scanExclusionService,
			OperationLockService operationLockService, @Value("#{jobParameters['sourcePath']}") String sourcePath,
			@Value("#{jobParameters['recursive']}") String recursive,
			@Value("#{jobParameters['includeHidden']}") String includeHidden) {
		this.fileScanner = fileScanner;
		this.operationLockService = operationLockService;
		this.sourcePath = PathUtils.normalizePath(sourcePath);
		this.scanOptions = new ScanOptions(Boolean.parseBoolean(recursive), Boolean.parseBoolean(includeHidden),
				List.of(), scanExclusionService.excludedExtensions(), scanExclusionService.excludedFolders());
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		try {
			lock = operationLockService.acquire(ExecutionType.INVENTORY, sourcePath);
		} catch (OperationLockException e) {
			// Another conflicting operation (typically an organization moving files on the
			// same tree) holds the path. Skip this run cleanly - emit no items so the step
			// finishes normally - instead of failing the batch job with a stack trace. This
			// mostly happens when the automatic folder-watch inventory races an
			// organization.
			log.info("Inventory skipped — {}", e.getMessage());

			iterator = Collections.emptyIterator();

			return;
		}

		try {
			stream = fileScanner.stream(sourcePath, scanOptions);

			iterator = stream.iterator();
		} catch (RuntimeException e) {
			lock.close();

			lock = null;

			throw e;
		}
	}

	@Override
	public Path read() {
		return iterator != null && iterator.hasNext() ? iterator.next() : null;
	}

	@Override
	public void update(ExecutionContext executionContext) {
		// No checkpoint tracking: a partially-read folder walk isn't meaningfully
		// resumable by
		// position, and every launch uses a fresh executionId/JobInstance anyway (see
		// InventoryBatchLauncherService).
	}

	@Override
	public void close() throws ItemStreamException {
		// Release the operation lock even if closing the folder-walk stream throws:
		// otherwise a failed stream.close() would leave the INVENTORY lock held until
		// the JVM restarts, blocking every future inventory/organization on that tree.
		try {
			if (stream != null) {
				stream.close();
			}
		} finally {
			if (lock != null) {
				lock.close();
			}
		}
	}
}