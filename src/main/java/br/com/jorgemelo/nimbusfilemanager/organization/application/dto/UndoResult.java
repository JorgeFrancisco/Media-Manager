package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import br.com.jorgemelo.nimbusfilemanager.organization.domain.enums.UndoStatus;

public record UndoResult(UndoStatus status, String message) {
}