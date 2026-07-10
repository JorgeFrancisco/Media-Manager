package br.com.jorgemelo.nimbusfilemanager.execution.application;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionType;
import br.com.jorgemelo.nimbusfilemanager.shared.util.PathUtils;

@Service
public class OperationLockService {

	private final Map<String, OperationLock> activeLocks = new ConcurrentHashMap<>();

	public OperationLock acquire(ExecutionType executionType, Path... paths) {
		Set<String> requestedPaths = normalizedPaths(paths);

		synchronized (activeLocks) {
			long currentThreadId = Thread.currentThread().threadId();

			activeLocks.values().stream()
					.filter(lock -> lock.ownerThreadId() != currentThreadId && lock.conflictsWith(requestedPaths))
					.findFirst().ifPresent(lock -> {
						throw new OperationLockException("Another " + lock.executionType()
								+ " execution is already running for path: " + lock.displayPath());
					});

			String lockId = UUID.randomUUID().toString();

			OperationLock lock = new OperationLock(lockId, currentThreadId, executionType, requestedPaths,
					() -> release(lockId));

			activeLocks.put(lock.id(), lock);

			return lock;
		}
	}

	/**
	 * Non-throwing check used by background coordination (the folder watcher) to
	 * decide whether to even start a reconcile/inventory this cycle. Returns true
	 * when another thread already holds a lock that conflicts with {@code paths}.
	 * Same-thread reentrancy is ignored, mirroring {@link #acquire}.
	 */
	public boolean isBusy(Path... paths) {
		Set<String> requestedPaths = normalizedPaths(paths);

		synchronized (activeLocks) {
			long currentThreadId = Thread.currentThread().threadId();

			return activeLocks.values().stream()
					.anyMatch(lock -> lock.ownerThreadId() != currentThreadId && lock.conflictsWith(requestedPaths));
		}
	}

	private Set<String> normalizedPaths(Path[] paths) {
		Set<String> normalized = new LinkedHashSet<>();

		for (Path path : paths) {
			if (path != null) {
				normalized.add(PathUtils.normalize(path).toLowerCase(Locale.ROOT));
			}
		}

		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("At least one path is required to acquire an operation lock.");
		}

		return normalized;
	}

	private void release(String lockId) {
		synchronized (activeLocks) {
			activeLocks.remove(lockId);
		}
	}
}