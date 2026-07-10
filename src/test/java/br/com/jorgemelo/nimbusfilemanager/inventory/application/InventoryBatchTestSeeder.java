package br.com.jorgemelo.nimbusfilemanager.inventory.application;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.core.task.SyncTaskExecutor;

import br.com.jorgemelo.nimbusfilemanager.execution.application.ExecutionMapper;
import br.com.jorgemelo.nimbusfilemanager.execution.application.constants.ExecutionMessages;
import br.com.jorgemelo.nimbusfilemanager.execution.application.dto.ExecutionResponse;
import br.com.jorgemelo.nimbusfilemanager.inventory.application.dto.InventoryRequest;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionStatus;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionType;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.Execution;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.model.StatusMessage;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.repository.ExecutionRepository;

/**
 * Seeds the catalog synchronously in integration tests by driving the real
 * Spring Batch inventory job to completion, exercising the exact production
 * path. Creates the {@link Execution} row and builds the {@link JobParameters}
 * the same way {@code InventoryBatchLauncherService#launch} does, then runs the
 * job to completion so a test can assert on the seeded state right after the
 * call returns.
 *
 * <p>
 * The production {@link JobLauncher} bean runs jobs on an async
 * {@code TaskExecutor} (so the web request can return immediately), which would
 * make {@code run} return before the job finished. This seeder therefore uses
 * its own launcher backed by a {@link SyncTaskExecutor}, so {@code run} blocks
 * until the job - and its {@code afterJob} listener that commits the final
 * counts - has finished.
 */
@TestComponent
public class InventoryBatchTestSeeder {

	private final JobLauncher jobLauncher;
	private final Job inventoryJob;
	private final ExecutionRepository executionRepository;
	private final ExecutionMapper executionMapper;
	private final String applicationVersion;
	private final Clock clock;

	public InventoryBatchTestSeeder(JobRepository jobRepository, Job inventoryJob,
			ExecutionRepository executionRepository, ExecutionMapper executionMapper,
			@Value("${application.version}") String applicationVersion, Clock clock) {
		this.jobLauncher = synchronousLauncher(jobRepository);
		this.inventoryJob = inventoryJob;
		this.executionRepository = executionRepository;
		this.executionMapper = executionMapper;
		this.applicationVersion = applicationVersion;
		this.clock = clock;
	}

	public ExecutionResponse seed(InventoryRequest request) throws Exception {
		Execution startedExecution = Execution.builder().executionType(ExecutionType.INVENTORY)
				.status(ExecutionStatus.STARTED).startedAt(LocalDateTime.now(clock))
				.sourcePath(request.source().toString()).targetPath(null).recursive(request.recursive())
				.executeFlag(true).applicationVersion(applicationVersion)
				.statusMessage(StatusMessage.code(ExecutionMessages.INVENTORY_STARTED)).build();

		Execution execution = executionRepository.save(startedExecution);

		Long executionId = execution.getId();

		JobParameters jobParameters = new JobParametersBuilder().addLong("executionId", executionId)
				.addString("sourcePath", request.source().toString())
				.addString("recursive", Boolean.toString(request.recursive()))
				.addString("includeHidden", Boolean.toString(request.includeHidden()))
				.addString("calculateHashes", Boolean.toString(request.calculateHashes()))
				.addString("forceAnalysis", Boolean.toString(request.forceAnalysis())).toJobParameters();

		jobLauncher.run(inventoryJob, jobParameters);

		return executionMapper.toResponse(executionRepository.findById(executionId)
				.orElseThrow(() -> new IllegalStateException("Execution not found: " + executionId)));
	}

	private static JobLauncher synchronousLauncher(JobRepository jobRepository) {
		TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();

		launcher.setJobRepository(jobRepository);
		launcher.setTaskExecutor(new SyncTaskExecutor());

		try {
			launcher.afterPropertiesSet();
		} catch (Exception e) {
			throw new IllegalStateException("Could not build synchronous job launcher for seeding", e);
		}

		return launcher;
	}
}