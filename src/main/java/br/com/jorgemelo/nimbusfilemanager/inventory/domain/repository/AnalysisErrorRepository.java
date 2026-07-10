package br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.jorgemelo.nimbusfilemanager.inventory.domain.enums.AnalysisErrorType;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.model.AnalysisError;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection.AnalysisErrorSummaryResponse;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection.ErrorFileDetailsResponse;
import br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection.ErrorStatisticsResponse;

public interface AnalysisErrorRepository extends JpaRepository<AnalysisError, Long> {

	List<AnalysisError> findByExecutionIdOrderByCreatedAtAsc(Long executionId);

	@Query("""
			SELECT new br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection.AnalysisErrorSummaryResponse(
				CAST(e.errorType AS string),
				COUNT(e)
			)
			FROM AnalysisError e
			WHERE e.execution.id = :executionId
			GROUP BY e.errorType
			ORDER BY COUNT(e) DESC
			""")
	List<AnalysisErrorSummaryResponse> summarizeByExecutionId(Long executionId);

	@Query("""
			SELECT new br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection.ErrorStatisticsResponse(
				CAST(e.errorType AS string),
				COUNT(e)
			)
			FROM AnalysisError e
			GROUP BY e.errorType
			ORDER BY COUNT(e) DESC
			""")
	List<ErrorStatisticsResponse> summarize();

	@Query("""
			SELECT new br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection.ErrorStatisticsResponse(
				CAST(e.errorType AS string),
				COUNT(DISTINCT e.path)
			)
			FROM AnalysisError e
			GROUP BY e.errorType
			ORDER BY COUNT(DISTINCT e.path) DESC
			""")
	List<ErrorStatisticsResponse> summarizeDistinctFiles();

	@Query("""
			SELECT new br.com.jorgemelo.nimbusfilemanager.inventory.domain.repository.projection.ErrorFileDetailsResponse(
				e.path,
				CAST(e.errorType AS string),
				COUNT(e),
				MIN(e.createdAt),
				MAX(e.createdAt)
			)
			FROM AnalysisError e
			WHERE (:errorType IS NULL OR e.errorType = :errorType)
			  AND (:path IS NULL OR LOWER(e.path) LIKE LOWER(CONCAT('%', :path, '%')))
			GROUP BY e.path, e.errorType
			ORDER BY COUNT(e) DESC, MAX(e.createdAt) DESC
			""")
	Page<ErrorFileDetailsResponse> findErrorFileDetails(AnalysisErrorType errorType, String path, Pageable pageable);
}