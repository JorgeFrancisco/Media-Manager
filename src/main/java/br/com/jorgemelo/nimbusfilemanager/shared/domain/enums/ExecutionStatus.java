package br.com.jorgemelo.nimbusfilemanager.shared.domain.enums;

public enum ExecutionStatus {

	STARTED, SCANNING_FILES, PROCESSING_FILES,

	FINISHED, FINISHED_WITH_ERRORS,

	INTERRUPTED, ERROR, CANCELLED,

	/**
	 * The execution was refused before any file was moved because the plan
	 * contained conflicts and conflicts were not allowed. It is a deliberate, safe
	 * no-op - not a failure - so it is shown as a warning ("Rejeitado"), never with
	 * the red error styling. {@code errors} stays 0 for this state.
	 */
	REJECTED;

	/**
	 * Single source of truth for "the execution has stopped for good". The three
	 * active states (STARTED, SCANNING_FILES, PROCESSING_FILES) are the only
	 * non-terminal ones; everything else is a final outcome. Callers (mapper, UI
	 * flags) rely on this instead of re-listing the terminal set.
	 */
	public boolean isTerminal() {
		return this == FINISHED || this == FINISHED_WITH_ERRORS || this == INTERRUPTED || this == ERROR
				|| this == CANCELLED || this == REJECTED;
	}
}