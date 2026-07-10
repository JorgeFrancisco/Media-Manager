package br.com.jorgemelo.nimbusfilemanager.geolocation.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto.LocationRebuildResult;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.LocationRebuildScope;

/**
 * Background rebuild runner: single-flight start guard, candidate counting,
 * result/error capture and the running flag lifecycle.
 */
class LocationRebuildAsyncRunnerTest {

	private final LocationRebuildService locationRebuildService = mock(LocationRebuildService.class);
	private final LocationRebuildAsyncRunner runner = new LocationRebuildAsyncRunner(locationRebuildService);

	@Test
	void startCountsCandidatesAndMarksRunning() {
		when(locationRebuildService.countCandidates(LocationRebuildScope.PENDING)).thenReturn(42L);

		Assertions.assertThat(runner.start(LocationRebuildScope.PENDING)).isTrue();
		Assertions.assertThat(runner.isRunning()).isTrue();
		Assertions.assertThat(runner.total()).isEqualTo(42);
		Assertions.assertThat(runner.processed()).isZero();
	}

	@Test
	void startReturnsFalseWhenAlreadyRunning() {
		when(locationRebuildService.countCandidates(any())).thenReturn(1L);

		Assertions.assertThat(runner.start(LocationRebuildScope.ALL)).isTrue();
		Assertions.assertThat(runner.start(LocationRebuildScope.ALL)).isFalse();
	}

	@Test
	void startTreatsCountingFailureAsZeroTotal() {
		when(locationRebuildService.countCandidates(any())).thenThrow(new RuntimeException("db down"));

		Assertions.assertThat(runner.start(LocationRebuildScope.LOW_CONFIDENCE)).isTrue();
		Assertions.assertThat(runner.total()).isZero();
	}

	@Test
	void rebuildStoresResultAndClearsRunningFlag() {
		when(locationRebuildService.countCandidates(any())).thenReturn(5L);

		LocationRebuildResult result = new LocationRebuildResult(LocationRebuildScope.ALL, 5, 4, 1, 0);

		when(locationRebuildService.rebuild(eq(LocationRebuildScope.ALL), any())).thenReturn(result);

		runner.start(LocationRebuildScope.ALL);
		runner.rebuild(LocationRebuildScope.ALL);

		Assertions.assertThat(runner.lastResult()).isSameAs(result);
		Assertions.assertThat(runner.lastError()).isNull();
		Assertions.assertThat(runner.isRunning()).isFalse();
	}

	@Test
	void rebuildRecordsErrorMessageOnFailure() {
		when(locationRebuildService.rebuild(any(), any())).thenThrow(new RuntimeException("boom"));

		runner.rebuild(LocationRebuildScope.PENDING);

		Assertions.assertThat(runner.lastError()).isEqualTo("boom");
		Assertions.assertThat(runner.isRunning()).isFalse();
	}

	@Test
	void progressAndEtaAreComputedFromCounters() {
		when(locationRebuildService.countCandidates(any())).thenReturn(0L);

		runner.start(LocationRebuildScope.PENDING);

		// total 0 -> percentage/eta are "unknown" (-1) per ProgressMath.
		Assertions.assertThat(runner.percent()).isEqualTo(-1);
		Assertions.assertThat(runner.etaSeconds()).isEqualTo(-1);
	}
}