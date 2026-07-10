package br.com.jorgemelo.nimbusfilemanager.metadata.application.dto;

import java.time.LocalDateTime;

public record PhotoMetadata(

		Integer width, Integer height,

		String manufacturer, String model,

		Integer orientationCode,

		Double latitude, Double longitude,

		Integer iso, String flash,

		String exposureTime, String fNumber, String focalLength, String lensModel, String whiteBalance,
		String exposureMode, String exposureProgram, String meteringMode,

		LocalDateTime captureDate,

		String exifJson) {
}