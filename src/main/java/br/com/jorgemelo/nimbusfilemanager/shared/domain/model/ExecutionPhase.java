package br.com.jorgemelo.nimbusfilemanager.shared.domain.model;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.ExecutionPhaseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Total wall time (and item count) spent in one {@link ExecutionPhaseType} of a
 * given execution. Purely a telemetry read-model; linked to {@code execution}
 * by id, not by a managed collection, so persisting it never loads the parent
 * graph.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "execution_phase")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExecutionPhase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name = "execution_id", nullable = false)
	private Long executionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ExecutionPhaseType phase;

	@Column(name = "duration_millis", nullable = false)
	private Long durationMillis;

	@Column(nullable = false)
	private Long items;
}