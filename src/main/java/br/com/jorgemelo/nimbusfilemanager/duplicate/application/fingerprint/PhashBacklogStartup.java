package br.com.jorgemelo.nimbusfilemanager.duplicate.application.fingerprint;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.FingerprintJobRunRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Primary resume mechanism: after a restart, any run left {@code RUNNING} by a
 * hard stop is marked failed and the backlog is kicked off again if work
 * remains (and no inventory is active - the runner's {@code start()} enforces
 * that). Opening the Duplicados screen is only a fallback to ensure the job is
 * active.
 *
 * <p>
 * Lives in its own bean so the calls to the {@code @Async} runner cross a proxy
 * boundary and actually run asynchronously.
 */
@Slf4j
@Component
public class PhashBacklogStartup {

	private final FingerprintJobRunRepository jobRunRepository;
	private final PhashBacklogAsyncRunner backlogRunner;
	private final Clock clock;

	public PhashBacklogStartup(FingerprintJobRunRepository jobRunRepository, PhashBacklogAsyncRunner backlogRunner,
			Clock clock) {
		this.jobRunRepository = jobRunRepository;
		this.backlogRunner = backlogRunner;
		this.clock = clock;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void resumeOnStartup() {
		int recovered = jobRunRepository.markRunningAsFailed(LocalDateTime.now(clock), "Interrupted by restart.");

		if (recovered > 0) {
			log.info("Recovered {} interrupted fingerprint job run(s) on startup.", recovered);
		}

		if (backlogRunner.start()) {
			backlogRunner.run();
		}
	}
}