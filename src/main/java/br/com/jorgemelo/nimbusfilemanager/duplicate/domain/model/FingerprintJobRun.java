package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model;

import java.time.LocalDateTime;

import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.FingerprintJobStatus;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.FingerprintKind;
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
 * Durable history/telemetry of one background fingerprint run - its OWN
 * structure, deliberately not the {@code execution} table (different lifecycle:
 * resumable, repeatable, no paths/movements/chunks). Live screen progress is
 * derived from the result/failure counts; this row records duration, throughput
 * and algorithm for before/after comparisons (e.g. when the JVM hash lands).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fingerprint_job_run")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FingerprintJobRun {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "kind", nullable = false, length = 30)
	private FingerprintKind kind;

	@Column(name = "algorithm", nullable = false, length = 40)
	private String algorithm;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private FingerprintJobStatus status;

	@Column(name = "started_at", nullable = false)
	private LocalDateTime startedAt;

	@Column(name = "finished_at")
	private LocalDateTime finishedAt;

	@Column(name = "processed", nullable = false)
	@Builder.Default
	private Long processed = 0L;

	@Column(name = "failed", nullable = false)
	@Builder.Default
	private Long failed = 0L;

	@Column(name = "total_at_start", nullable = false)
	@Builder.Default
	private Long totalAtStart = 0L;

	@Column(name = "message")
	private String message;
}