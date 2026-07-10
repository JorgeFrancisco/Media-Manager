package br.com.jorgemelo.nimbusfilemanager.telemetry.application.dto;

import java.util.List;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionPhaseType;

/**
 * One phase across the compared executions: {@code durationsMillis} is aligned
 * to the comparison's execution order (a 0 means the phase did not occur in
 * that execution), so a template can render a grouped bar per execution side by
 * side.
 */
public record PhaseComparisonRow(ExecutionPhaseType phase, List<Long> durationsMillis) {
}