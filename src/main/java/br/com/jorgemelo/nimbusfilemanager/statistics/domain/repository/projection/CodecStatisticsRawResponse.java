package br.com.jorgemelo.nimbusfilemanager.statistics.domain.repository.projection;

public record CodecStatisticsRawResponse(String codec, long files, double percentage, long totalSizeBytes) {
}