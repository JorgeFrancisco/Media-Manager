package br.com.jorgemelo.nimbusfilemanager.telemetry.application.dto;

import java.util.List;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.repository.projection.ExecutionTelemetryRow;

/**
 * Side-by-side comparison of two or more executions for the Statistics screen:
 * the selected executions (in a stable order) plus, for each phase that
 * occurred in any of them, the per-execution durations aligned to that same
 * order. {@code maxPhaseMillis} is the largest single phase duration across the
 * whole comparison, so bars can be scaled consistently.
 */
public record ExecutionComparison(List<ExecutionTelemetryRow> executions, List<PhaseComparisonRow> phases,
		long maxPhaseMillis) {

	public boolean isEmpty() {
		return executions.isEmpty();
	}
}