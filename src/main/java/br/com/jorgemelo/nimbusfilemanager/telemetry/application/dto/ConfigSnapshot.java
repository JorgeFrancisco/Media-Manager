package br.com.jorgemelo.nimbusfilemanager.telemetry.application.dto;

public record ConfigSnapshot(Integer workers, Integer chunkSize, Integer ffmpegPhotoHashLimit,
		Integer ffprobeVideoLimit) {
}