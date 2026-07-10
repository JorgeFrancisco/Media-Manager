package br.com.jorgemelo.nimbusfilemanager.inventory.application.batch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

class InventoryJobConfigTest {

	private final InventoryJobConfig config = new InventoryJobConfig();
	private final JobRepository jobRepository = mock(JobRepository.class);

	@Test
	void inventoryStepShouldBeChunkOrientedWithConfiguredReaderProcessorWriter() {
		@SuppressWarnings("unchecked")
		ItemStreamReader<Path> reader = mock(ItemStreamReader.class);

		@SuppressWarnings("unchecked")
		ItemProcessor<Path, Path> processor = mock(ItemProcessor.class);

		@SuppressWarnings("unchecked")
		ItemWriter<Path> writer = mock(ItemWriter.class);

		PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

		Step step = config.inventoryStep(jobRepository, transactionManager, reader, processor, writer);

		Assertions.assertThat(step).isNotNull();
		Assertions.assertThat(step.getName()).isEqualTo("inventoryStep");
	}

	@Test
	void inventoryJobShouldStartWithInventoryStepAndRegisterTheListener() {
		Step step = mock(Step.class);

		when(step.getName()).thenReturn("inventoryStep");

		InventoryJobExecutionListener listener = mock(InventoryJobExecutionListener.class);

		Job job = config.inventoryJob(jobRepository, step, listener);

		Assertions.assertThat(job).isNotNull();
		Assertions.assertThat(job.getName()).isEqualTo("inventoryJob");
	}
}