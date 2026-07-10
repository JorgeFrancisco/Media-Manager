package br.com.jorgemelo.nimbusfilemanager.duplicate.application;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class PhotoSimilarityAsyncRunnerTest {

	private final PhotoSimilarityService service = mock(PhotoSimilarityService.class);
	private final PhotoSimilarityAsyncRunner runner = new PhotoSimilarityAsyncRunner(service);

	@Test
	void doesNotClaimWorkWhenAlreadyCached() {
		when(service.isCached(70)).thenReturn(true);

		Assertions.assertThat(runner.start(70)).isFalse();
		Assertions.assertThat(runner.isRunning()).isFalse();
	}

	@Test
	void claimsWorkAndTracksProgressWhileRunning() {
		when(service.isCached(70)).thenReturn(false);
		doAnswer(invocation -> {
			SimilarityProgressCallback callback = invocation.getArgument(1);
			callback.update(5, 10);
			return null;
		}).when(service).computeAndCache(eq(70), ArgumentMatchers.any());

		Assertions.assertThat(runner.start(70)).isTrue();

		runner.run(70);

		Assertions.assertThat(runner.processed()).isEqualTo(5);
		Assertions.assertThat(runner.total()).isEqualTo(10);
		Assertions.assertThat(runner.percent()).isEqualTo(50);
		Assertions.assertThat(runner.isRunning()).isFalse();

		verify(service).computeAndCache(eq(70), ArgumentMatchers.any());
	}

	@Test
	void doesNotClaimASecondRunWhileOneIsInProgress() {
		when(service.isCached(70)).thenReturn(false);

		Assertions.assertThat(runner.start(70)).isTrue();
		Assertions.assertThat(runner.start(70)).isFalse();

		verify(service, never()).computeAndCache(ArgumentMatchers.anyInt(), ArgumentMatchers.any());
	}

	@Test
	void releasesTheLockEvenWhenTheGroupingFails() {
		when(service.isCached(70)).thenReturn(false);
		doThrow(new RuntimeException("boom")).when(service).computeAndCache(eq(70), ArgumentMatchers.any());

		Assertions.assertThat(runner.start(70)).isTrue();

		runner.run(70);

		Assertions.assertThat(runner.isRunning()).isFalse();
	}

	@Test
	void percentIsZeroBeforeTheTotalIsKnown() {
		Assertions.assertThat(runner.percent()).isZero();
	}
}