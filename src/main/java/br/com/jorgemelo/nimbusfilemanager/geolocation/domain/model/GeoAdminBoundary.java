package br.com.jorgemelo.nimbusfilemanager.geolocation.domain.model;

import br.com.jorgemelo.nimbusfilemanager.geolocation.domain.enums.AdminBoundaryKind;
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
 * One administrative boundary (country, state or municipality) of the imported
 * offline dataset. The polygon is stored as WKB in a plain {@code bytea} column
 * - no PostGIS. The bounding box columns let the resolver prefilter or lazily
 * load by region; the actual Point-in-Polygon test happens in memory (JTS). The
 * rest of the application never queries this table directly.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "geo_admin_boundary")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GeoAdminBoundary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AdminBoundaryKind kind;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(name = "country_code", nullable = false, length = 2)
	private String countryCode;

	@Column(name = "country_name", length = 120)
	private String countryName;

	@Column(name = "state_name", length = 120)
	private String stateName;

	@Column(name = "min_lat", nullable = false)
	private double minLat;

	@Column(name = "min_lon", nullable = false)
	private double minLon;

	@Column(name = "max_lat", nullable = false)
	private double maxLat;

	@Column(name = "max_lon", nullable = false)
	private double maxLon;

	/** Polygon/multipolygon in Well-Known Binary (WKB). */
	@Column(name = "geometry", nullable = false, columnDefinition = "bytea")
	private byte[] geometry;

	@Column(nullable = false, length = 40)
	private String source;

	@Column(name = "dataset_version", nullable = false, length = 50)
	private String datasetVersion;
}