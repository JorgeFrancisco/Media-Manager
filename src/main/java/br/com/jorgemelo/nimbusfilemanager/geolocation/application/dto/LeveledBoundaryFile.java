package br.com.jorgemelo.nimbusfilemanager.geolocation.application.dto;

import java.nio.file.Path;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.AdminBoundaryKind;

public record LeveledBoundaryFile(AdminBoundaryKind kind, Path file) {
}