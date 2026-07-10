package br.com.jorgemelo.nimbusfilemanager.execution.application;

import java.util.Set;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionType;

public final class OperationLock implements AutoCloseable {

	private final String id;
	private final long ownerThreadId;
	private final ExecutionType executionType;
	private final Set<String> paths;
	private final Runnable releaseAction;

	OperationLock(String id, long ownerThreadId, ExecutionType executionType, Set<String> paths,
			Runnable releaseAction) {
		this.id = id;
		this.ownerThreadId = ownerThreadId;
		this.executionType = executionType;
		this.paths = paths;
		this.releaseAction = releaseAction;
	}

	String id() {
		return id;
	}

	ExecutionType executionType() {
		return executionType;
	}

	long ownerThreadId() {
		return ownerThreadId;
	}

	String displayPath() {
		return paths.iterator().next();
	}

	boolean conflictsWith(Set<String> requestedPaths) {
		return paths.stream()
				.anyMatch(current -> requestedPaths.stream().anyMatch(requested -> overlaps(current, requested)));
	}

	private boolean overlaps(String first, String second) {
		return first.equals(second) || first.startsWith(second + "\\") || second.startsWith(first + "\\")
				|| first.startsWith(second + "/") || second.startsWith(first + "/");
	}

	@Override
	public void close() {
		releaseAction.run();
	}
}