package br.com.jorgemelo.nimbusfilemanager.quality.dto;

public record CoverageTarget(String className, long missed, long covered, double percentage) {
}