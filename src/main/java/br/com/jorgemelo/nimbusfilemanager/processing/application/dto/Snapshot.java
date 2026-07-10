package br.com.jorgemelo.nimbusfilemanager.processing.application.dto;

import java.util.Map;

import br.com.jorgemelo.nimbusfilemanager.processing.domain.enums.ExternalToolCategory;

public record Snapshot(long tasksExecuted, long tasksCacheAvoided, long tasksCancelled, long tasksError,
		long queueWaitNanos, long taskTotalNanos, long jvmExtractionNanos, long persistenceNanos, long wallClockNanos,
		long photoHashJvmDecodable, long photoHashFfmpegOnly, long photoHashFailures, int maxConcurrency,
		Map<ExternalToolCategory, CategorySnapshot> categories) {
}