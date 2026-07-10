package br.com.jorgemelo.nimbusfilemanager.geolocation.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
 * Persistent reverse-geocoding cache keyed by rounded coordinates (~11 m), so
 * near-identical coordinates are resolved once instead of thousands of times.
 *
 * <p>
 * The resolved place itself lives in the shared {@link ResolvedPlace}
 * embeddable, kept in lock-step with {@link MediaGeoLocation}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "geo_resolution_cache")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GeoResolutionCache {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name = "cache_key", nullable = false, unique = true, length = 50)
	private String cacheKey;

	@Embedded
	private ResolvedPlace place;
}