package br.com.jorgemelo.nimbusfilemanager.quarantine.application.dto;

import java.util.List;
import java.util.UUID;

public record QuarantineRestoreSelectedRequest(List<UUID> ids) {
}