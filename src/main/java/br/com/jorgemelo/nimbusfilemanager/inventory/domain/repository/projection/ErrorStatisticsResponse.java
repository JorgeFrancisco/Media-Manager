package br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection;

public record ErrorStatisticsResponse(String errorType, long count) {
}