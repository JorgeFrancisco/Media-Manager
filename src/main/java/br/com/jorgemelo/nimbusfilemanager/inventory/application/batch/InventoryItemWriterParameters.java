package br.com.jorgemelo.nimbusfilemanager.inventory.application.batch;

import java.nio.file.Path;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.MetadataOptions;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;

/**
 * Step-scoped holder for the inventory writer's job-parameter values. Binding
 * them here as a single collaborator keeps {@link InventoryItemWriter} within
 * the constructor-parameter limit while leaving its injected beans untouched.
 * The values only exist inside the running step, hence {@code @StepScope}.
 */
@Component
@StepScope
public class InventoryItemWriterParameters {

	private final Path sourcePath;
	private final MetadataOptions metadataOptions;
	private final Long executionId;

	public InventoryItemWriterParameters(@Value("#{jobParameters['sourcePath']}") String sourcePath,
			@Value("#{jobParameters['calculateHashes']}") String calculateHashes,
			@Value("#{jobParameters['forceAnalysis']}") String forceAnalysis,
			@Value("#{jobParameters['executionId']}") Long executionId) {
		this.sourcePath = PathUtils.normalizePath(sourcePath);
		this.metadataOptions = new MetadataOptions(Boolean.parseBoolean(calculateHashes),
				Boolean.parseBoolean(forceAnalysis));
		this.executionId = executionId;
	}

	public Path sourcePath() {
		return sourcePath;
	}

	public MetadataOptions metadataOptions() {
		return metadataOptions;
	}

	public Long executionId() {
		return executionId;
	}
}