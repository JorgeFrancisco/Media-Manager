package br.com.jorgemelo.nimbusfilemanager.quality.dto;

public record CoverageSummary(double instruction, double branch, double line, double method, double clazz) {
}