package br.com.jorgemelo.nimbusfilemanager.inventory.application.batch;

import java.nio.file.Path;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires the inventory {@link Job}: a single chunk-oriented {@link Step} using
 * {@link InventoryFileItemReader} / {@link InventoryItemProcessor} /
 * {@link InventoryItemWriter}. {@code spring.batch.job.enabled=false} keeps
 * Spring Boot from auto-running this Job at startup; it is only launched
 * explicitly by {@link InventoryBatchLauncherService}.
 */
@Configuration
public class InventoryJobConfig {

	/**
	 * Files persisted per DB round-trip (existence-check query + save), same order
	 * of magnitude as the batching cap the old streaming scanner used.
	 */
	static final int CHUNK_SIZE = 200;

	@Bean
	public Job inventoryJob(JobRepository jobRepository, Step inventoryStep, InventoryJobExecutionListener listener) {
		return new JobBuilder("inventoryJob", jobRepository).listener(listener).start(inventoryStep).build();
	}

	@Bean
	public Step inventoryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			ItemStreamReader<Path> inventoryFileItemReader, ItemProcessor<Path, Path> inventoryItemProcessor,
			ItemWriter<Path> inventoryItemWriter) {
		return new StepBuilder("inventoryStep", jobRepository).<Path, Path>chunk(CHUNK_SIZE, transactionManager)
				.reader(inventoryFileItemReader).processor(inventoryItemProcessor).writer(inventoryItemWriter).build();
	}
}