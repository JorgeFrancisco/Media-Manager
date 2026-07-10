package br.com.jorgemelo.nimbusfilemanager.statistics.application.dto;

import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.SizeResponse;

public record CodecStatisticsResponse(String codec, long files, double percentage, SizeResponse totalSize) {
}