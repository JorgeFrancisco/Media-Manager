package br.com.jorgemelo.nimbusfilemanager.execution.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Tells whether an inventory scan is actually executing (an active execution of
 * type INVENTORY) - not merely whether the folder monitor is watching. Several
 * settings actions that read or write the media catalog block while it is true,
 * so the single "which execution type blocks" rule lives here instead of being
 * copied into every caller.
 */
@Component
public class InventoryRunningState {

	private final ExecutionQueryService executionQueryService;

	@Autowired
	public InventoryRunningState(ExecutionQueryService executionQueryService) {
		this.executionQueryService = executionQueryService;
	}

	public boolean isRunning() {
		return executionQueryService.active().map(execution -> "INVENTORY".equals(execution.executionType()))
				.orElse(false);
	}
}