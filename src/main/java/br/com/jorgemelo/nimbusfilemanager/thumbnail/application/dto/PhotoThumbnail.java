package br.com.jorgemelo.nimbusfilemanager.thumbnail.application.dto;

import java.nio.file.Path;

public record PhotoThumbnail(Path path, String etag) {
}