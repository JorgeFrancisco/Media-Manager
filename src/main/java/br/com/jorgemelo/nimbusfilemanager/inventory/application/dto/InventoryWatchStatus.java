package br.com.jorgemelo.nimbusfilemanager.inventory.application.dto;

import java.time.LocalDateTime;

public record InventoryWatchStatus(String folder, boolean configured, boolean running, LocalDateTime lastEvent,
		String error, LocalDateTime lastReconciliation, long lastReconciliationRepaired) {

	public static InventoryWatchStatus unconfigured() {
		return new InventoryWatchStatus("", false, false, null, null, null, 0);
	}
}