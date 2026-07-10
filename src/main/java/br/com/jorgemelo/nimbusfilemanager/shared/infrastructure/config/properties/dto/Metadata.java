package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto;

public record Metadata(Exiftool exiftool, Mediainfo mediainfo, Ffprobe ffprobe) {
}