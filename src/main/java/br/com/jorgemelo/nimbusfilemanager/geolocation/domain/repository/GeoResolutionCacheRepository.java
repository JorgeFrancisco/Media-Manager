package br.com.jorgemelo.nimbusfilemanager.geolocation.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.model.GeoResolutionCache;

public interface GeoResolutionCacheRepository extends JpaRepository<GeoResolutionCache, Long> {

	Optional<GeoResolutionCache> findByCacheKey(String cacheKey);

	/**
	 * Idempotent cache write. The rebuild resolves media in parallel, so several
	 * threads routinely resolve the very same rounded coordinates at once and race
	 * to insert one {@code cache_key}. A plain {@code save} lets the second insert
	 * hit the unique constraint, and in PostgreSQL that aborts the whole
	 * transaction (SQLState 25P02) - poisoning the surrounding media persist.
	 * {@code ON CONFLICT DO NOTHING} makes the loser a no-op instead of an error,
	 * so no exception is ever raised. Enums are bound by name to match their
	 * {@code EnumType.STRING} columns.
	 */
	@Modifying
	@Query(nativeQuery = true, value = """
			insert into geo_resolution_cache (cache_key, country_code, country_name, state_name, city_name,
			        distance_km, confidence, provider, dataset_version, resolved_at)
			values (:#{#entry.cacheKey}, :#{#entry.place.countryCode}, :#{#entry.place.countryName},
			        :#{#entry.place.stateName}, :#{#entry.place.cityName}, :#{#entry.place.distanceKm},
			        :#{#entry.place.confidence.name()}, :#{#entry.place.provider.name()},
			        :#{#entry.place.datasetVersion}, :#{#entry.place.resolvedAt})
			on conflict (cache_key) do nothing
			""")
	int insertIgnoringDuplicate(@Param("entry") GeoResolutionCache entry);
}