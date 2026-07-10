package br.com.jorgemelo.nimbusfilemanager.duplicate.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto.DuplicateDeletionResult;

class DuplicateDeletionAsyncRunnerTest {

	private final DuplicateDeletionService service = mock(DuplicateDeletionService.class);
	private final DuplicateDeletionAsyncRunner runner = new DuplicateDeletionAsyncRunner(service);

	@Test
	void claimsWorkTracksProgressAndKeepsTheResult() {
		List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

		DuplicateDeletionResult result = new DuplicateDeletionResult(true, 2, 2, 0, 0, UUID.randomUUID(), "ok");

		doAnswer(invocation -> {
			DeletionProgressCallback callback = invocation.getArgument(1);

			callback.update(1, 2);
			callback.update(2, 2);

			return result;
		}).when(service).delete(any(), any());

		Assertions.assertThat(runner.start(2)).isTrue();

		runner.run(ids);

		Assertions.assertThat(runner.processed()).isEqualTo(2);
		Assertions.assertThat(runner.total()).isEqualTo(2);
		Assertions.assertThat(runner.percent()).isEqualTo(100);
		Assertions.assertThat(runner.isRunning()).isFalse();
		Assertions.assertThat(runner.lastResult()).isSameAs(result);
	}

	@Test
	void doesNotClaimASecondRunWhileOneIsInProgress() {
		Assertions.assertThat(runner.start(3)).isTrue();
		Assertions.assertThat(runner.start(3)).isFalse();

		verify(service, never()).delete(any(), any());
	}

	@Test
	void snapshotReflectsTheClaimedTotalBeforeAnyFileMoves() {
		runner.start(5);

		Assertions.assertThat(runner.isRunning()).isTrue();
		Assertions.assertThat(runner.processed()).isZero();
		Assertions.assertThat(runner.total()).isEqualTo(5);
		Assertions.assertThat(runner.percent()).isZero();
		Assertions.assertThat(runner.lastResult()).isNull();
	}

	@Test
	void releasesTheLockAndRecordsAFailureWhenTheMoveThrows() {
		doThrow(new RuntimeException("boom")).when(service).delete(any(), any());

		Assertions.assertThat(runner.start(2)).isTrue();

		runner.run(List.of(UUID.randomUUID(), UUID.randomUUID()));

		Assertions.assertThat(runner.isRunning()).isFalse();
		Assertions.assertThat(runner.lastResult()).isNotNull();
		Assertions.assertThat(runner.lastResult().errors()).isEqualTo(2);
		Assertions.assertThat(runner.lastResult().configured()).isTrue();
	}

	@Test
	void releaseRejectedSubmissionFreesTheClaimSoDeletionIsNotStuckForever() {
		// Simulates the controller path when the @Async submission of run() is rejected
		// (shared executor saturated or shutting down): start() claimed the run but the
		// run() body - and its finally that clears the flag - never executes. Without
		// releaseRejectedSubmission() the runner would stay running forever and refuse
		// every future deletion until an app restart.
		Assertions.assertThat(runner.start(3)).isTrue();
		Assertions.assertThat(runner.isRunning()).isTrue();

		runner.releaseRejectedSubmission();

		Assertions.assertThat(runner.isRunning()).isFalse();
		Assertions.assertThat(runner.lastResult()).isNotNull();
		Assertions.assertThat(runner.lastResult().errors()).isEqualTo(3);
		// The claim is free again, so a new deletion can be started.
		Assertions.assertThat(runner.start(1)).isTrue();

		verify(service, never()).delete(any(), any());
	}
}