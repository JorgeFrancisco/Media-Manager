package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model.FingerprintJobRun;

/**
 * Durable history of background fingerprint runs (its own structure, not the
 * {@code execution} table).
 */
public interface FingerprintJobRunRepository extends JpaRepository<FingerprintJobRun, Long> {

	Optional<FingerprintJobRun> findFirstByOrderByStartedAtDesc();

	/**
	 * Crash recovery on startup: a run left {@code RUNNING} by a hard stop is
	 * marked {@code FAILED}. Harmless if none - resume is driven by the derived
	 * pending queue, not by this row.
	 */
	@Transactional
	@Modifying
	@Query("""
			UPDATE FingerprintJobRun r
			SET r.status = br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.FingerprintJobStatus.FAILED,
			    r.finishedAt = :now, r.message = :message
			WHERE r.status = br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.FingerprintJobStatus.RUNNING
			""")
	int markRunningAsFailed(@Param("now") LocalDateTime now, @Param("message") String message);
}