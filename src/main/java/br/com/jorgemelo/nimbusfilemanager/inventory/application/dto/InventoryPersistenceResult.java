package br.com.jorgemelo.nimbusfilemanager.inventory.application.dto;

import br.com.jorgemelo.nimbusfilemanager.inventory.domain.enums.InventoryPersistenceAction;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.enums.ProcessResult;

public record InventoryPersistenceResult(ProcessResult result, InventoryPersistenceAction action) {
}