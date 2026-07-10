package br.com.jorgemelo.nimbusfilemanager.execution.application;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionType;

class OperationLockServiceTest {

	private final OperationLockService operationLockService = new OperationLockService();

	@Test
	void acquireShouldAllowNestedLockInSameThreadAndReleaseItAfterClose() {
		Path parent = Path.of("C:/media");
		Path child = Path.of("C:/media/2024");

		try (var _ = operationLockService.acquire(ExecutionType.INVENTORY, parent)) {
			try (var nested = operationLockService.acquire(ExecutionType.ORGANIZATION, child)) {
				Assertions.assertThat(nested).isNotNull();
			}
		}

		try (var ignored = operationLockService.acquire(ExecutionType.ORGANIZATION, child)) {
			Assertions.assertThat(ignored).isNotNull();
		}
	}

	@Test
	void acquireShouldAllowOnlyOneConcurrentExecutionForSamePath() throws Exception {
		CountDownLatch firstLockAcquired = new CountDownLatch(1);
		CountDownLatch releaseFirstLock = new CountDownLatch(1);

		AtomicReference<Throwable> firstThreadFailure = new AtomicReference<>();
		AtomicReference<Throwable> secondThreadFailure = new AtomicReference<>();

		Thread firstThread = new Thread(() -> {
			try (var _ = operationLockService.acquire(ExecutionType.INVENTORY, Path.of("C:/media"))) {
				firstLockAcquired.countDown();
				releaseFirstLock.await();
			} catch (Throwable e) {
				firstThreadFailure.set(e);
			}
		});

		Thread secondThread = new Thread(() -> {
			try {
				firstLockAcquired.await(2, TimeUnit.SECONDS);
				operationLockService.acquire(ExecutionType.ORGANIZATION, Path.of("C:/media"));
			} catch (Throwable e) {
				secondThreadFailure.set(e);
			}
		});

		firstThread.start();
		secondThread.start();
		secondThread.join();
		releaseFirstLock.countDown();
		firstThread.join();

		Assertions.assertThat(firstThreadFailure.get()).isNull();
		Assertions.assertThat(secondThreadFailure.get()).isInstanceOf(OperationLockException.class);
	}

	@Test
	void isBusyShouldReportConflictingLockHeldByAnotherThreadWithoutThrowing() throws Exception {
		CountDownLatch acquired = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);

		Thread holder = new Thread(() -> {
			try (var _ = operationLockService.acquire(ExecutionType.ORGANIZATION, Path.of("C:/media"))) {
				acquired.countDown();
				release.await(2, TimeUnit.SECONDS);
			} catch (Exception _) {
				// interrupted; nothing to do
			}
		});

		holder.start();

		Assertions.assertThat(acquired.await(2, TimeUnit.SECONDS)).isTrue();

		// Containment-aware: a lock on C:/media makes the whole subtree busy.
		Assertions.assertThat(operationLockService.isBusy(Path.of("C:/media/2024"))).isTrue();
		Assertions.assertThat(operationLockService.isBusy(Path.of("C:/other"))).isFalse();

		release.countDown();
		holder.join();

		Assertions.assertThat(operationLockService.isBusy(Path.of("C:/media"))).isFalse();
	}
}