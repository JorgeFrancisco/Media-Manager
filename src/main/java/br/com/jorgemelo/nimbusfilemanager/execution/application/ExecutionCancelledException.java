package br.com.jorgemelo.nimbusfilemanager.execution.application;

/**
 * Thrown from inside a long-running scan/organization loop when
 * {@link ExecutionCancellationService#isCancelled(Long)} comes back true, so
 * the loop can unwind through the normal exception handling instead of every
 * call site needing its own early-return plumbing. Callers that catch this
 * should record the execution as CANCELLED, not as an error.
 */
public class ExecutionCancelledException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExecutionCancelledException(String message) {
		super(message);
	}
}