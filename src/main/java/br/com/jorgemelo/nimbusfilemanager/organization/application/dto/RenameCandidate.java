package br.com.jorgemelo.nimbusfilemanager.organization.application.dto;

import java.nio.file.Path;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.FileSystemDates;

public record RenameCandidate(Path path, FileSystemDates dates) {
}