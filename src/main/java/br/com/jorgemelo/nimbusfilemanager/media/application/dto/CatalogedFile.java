package br.com.jorgemelo.nimbusfilemanager.media.application.dto;

import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

public record CatalogedFile(UUID publicId, FileType fileType) {
}