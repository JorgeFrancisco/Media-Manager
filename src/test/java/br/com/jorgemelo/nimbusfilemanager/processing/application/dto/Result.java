package br.com.jorgemelo.nimbusfilemanager.processing.application.dto;

import java.util.List;

public record Result(List<Outcome<Integer, Integer>> outcomes, long wallNanos, long accumulatedTaskNanos,
		int maxConcurrency) {
}