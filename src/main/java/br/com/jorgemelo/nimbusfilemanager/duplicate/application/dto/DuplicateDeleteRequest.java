package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import java.util.List;
import java.util.UUID;

public record DuplicateDeleteRequest(List<UUID> ids) {
}