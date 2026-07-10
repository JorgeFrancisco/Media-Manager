package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	public static final String TASK_EXECUTOR = "nimbusFileManagerTaskExecutor";
	public static final String GEOLOCATION_EXECUTOR = "nimbusFileManagerGeolocationExecutor";
	public static final String VISUAL_ANALYSIS_EXECUTOR = "nimbusFileManagerVisualAnalysisExecutor";

	@Bean(name = TASK_EXECUTOR)
	public Executor nimbusFileManagerTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("nimbus-file-manager-async-");
		executor.initialize();

		return executor;
	}

	@Bean(name = GEOLOCATION_EXECUTOR)
	public Executor nimbusFileManagerGeolocationExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("nimbus-file-manager-geolocation-");
		executor.initialize();

		return executor;
	}

	@Bean(name = VISUAL_ANALYSIS_EXECUTOR)
	public Executor nimbusFileManagerVisualAnalysisExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("nimbus-file-manager-visual-analysis-");
		executor.initialize();

		return executor;
	}
}