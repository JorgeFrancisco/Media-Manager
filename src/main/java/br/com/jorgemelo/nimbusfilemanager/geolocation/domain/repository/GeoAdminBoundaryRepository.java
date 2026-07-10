package br.com.jorgemelo.nimbusfilemanager.geolocation.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.AdminBoundaryKind;
import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.model.GeoAdminBoundary;

public interface GeoAdminBoundaryRepository extends JpaRepository<GeoAdminBoundary, Long> {

	long countByKind(AdminBoundaryKind kind);

	/**
	 * Bounding-box candidate search for a level: rows whose bbox contains the
	 * point, backed by the (min_lat, max_lat, min_lon, max_lon) index - the spatial
	 * prefilter step. Callers confirm containment with a Point-in-Polygon test in
	 * JTS; no PostGIS involved.
	 */
	@Query("""
			select b
			from GeoAdminBoundary b
			where b.kind = :kind
			  and :lat between b.minLat and b.maxLat
			  and :lon between b.minLon and b.maxLon
			""")
	List<GeoAdminBoundary> findCandidates(@Param("kind") AdminBoundaryKind kind, @Param("lat") double lat,
			@Param("lon") double lon);

	/** Bounding-box candidate search restricted to one country. */
	@Query("""
			select b
			from GeoAdminBoundary b
			where b.kind = :kind
			  and b.countryCode = :countryCode
			  and :lat between b.minLat and b.maxLat
			  and :lon between b.minLon and b.maxLon
			""")
	List<GeoAdminBoundary> findCandidatesInCountry(@Param("kind") AdminBoundaryKind kind,
			@Param("countryCode") String countryCode, @Param("lat") double lat, @Param("lon") double lon);

	/**
	 * Bounding-box candidate search for the nearest-boundary fallback: rows whose
	 * bbox expanded by the given margins contains the point. Used only when no
	 * polygon contains the coordinate (e.g. at sea near the coast), so the caller
	 * can measure the distance from the point to each candidate polygon.
	 */
	@Query("""
			select b
			from GeoAdminBoundary b
			where b.kind = :kind
			  and :lat between b.minLat - :latMargin and b.maxLat + :latMargin
			  and :lon between b.minLon - :lonMargin and b.maxLon + :lonMargin
			""")
	List<GeoAdminBoundary> findCandidatesNear(@Param("kind") AdminBoundaryKind kind, @Param("lat") double lat,
			@Param("lon") double lon, @Param("latMargin") double latMargin, @Param("lonMargin") double lonMargin);

	/**
	 * Distinct country codes present at one level; drives the territory
	 * gap-filling.
	 */
	@Query("select distinct b.countryCode from GeoAdminBoundary b where b.kind = :kind")
	List<String> findDistinctCountryCodes(@Param("kind") AdminBoundaryKind kind);

	/**
	 * Boundary lookups by name, used to place an approximate map pin at the
	 * representative point of an administrative region (deepest available level).
	 */
	Optional<GeoAdminBoundary> findFirstByKindAndCountryCodeIgnoreCaseAndStateNameIgnoreCaseAndNameIgnoreCase(
			AdminBoundaryKind kind, String countryCode, String stateName, String name);

	Optional<GeoAdminBoundary> findFirstByKindAndCountryCodeIgnoreCaseAndNameIgnoreCase(AdminBoundaryKind kind,
			String countryCode, String name);

	Optional<GeoAdminBoundary> findFirstByKindAndCountryCodeIgnoreCase(AdminBoundaryKind kind, String countryCode);

	@Modifying
	@Query("delete from GeoAdminBoundary b")
	int deleteAllRows();
}