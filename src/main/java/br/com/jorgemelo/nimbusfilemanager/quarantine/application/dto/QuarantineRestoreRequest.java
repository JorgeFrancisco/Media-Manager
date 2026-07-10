package br.com.jorgemelo.nimbusfilemanager.quarantine.application.dto;

import java.util.UUID;

public record QuarantineRestoreRequest(UUID movementId, String conflict, String destinationFolder) {
}