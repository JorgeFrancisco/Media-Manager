package br.com.jorgemelo.nimbusfilemanager.quarantine.application.dto;

public record MovementPurgeResult(boolean removed, Long catalogFileId) {

	public static MovementPurgeResult removed(Long catalogFileId) {
		return new MovementPurgeResult(true, catalogFileId);
	}

	public static MovementPurgeResult notRemoved() {
		return new MovementPurgeResult(false, null);
	}
}