package br.com.jorgemelo.nimbusfilemanager.duplicate.application.fingerprint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository.FingerprintJobRunRepository;

@ExtendWith(MockitoExtension.class)
class PhashBacklogStartupTest {

	@Mock
	private FingerprintJobRunRepository jobRunRepository;

	@Mock
	private PhashBacklogAsyncRunner backlogRunner;

	@Test
	void resumeRecoversStaleRunsAndStartsTheBacklog() {
		when(jobRunRepository.markRunningAsFailed(any(), eq("Interrupted by restart."))).thenReturn(2);
		when(backlogRunner.start()).thenReturn(true);

		new PhashBacklogStartup(jobRunRepository, backlogRunner, Clock.systemDefaultZone()).resumeOnStartup();

		verify(jobRunRepository).markRunningAsFailed(any(), eq("Interrupted by restart."));
		verify(backlogRunner).run();
	}

	@Test
	void resumeDoesNotRunWhenStartIsRefused() {
		when(backlogRunner.start()).thenReturn(false);

		new PhashBacklogStartup(jobRunRepository, backlogRunner, Clock.systemDefaultZone()).resumeOnStartup();

		verify(backlogRunner, never()).run();
	}
}