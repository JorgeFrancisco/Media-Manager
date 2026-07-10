package br.com.jorgemelo.nimbusfilemanager.shared.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "photo")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Photo {

	@Id
	@Column(name = "catalog_file_id")
	@EqualsAndHashCode.Include
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catalog_file_id")
	@ToString.Exclude
	private CatalogFile catalogFile;

	@Column(name = "format")
	private String format;

	@Column(name = "iso")
	private Integer iso;

	@Column(name = "flash")
	private String flash;

	@Column(name = "exposure_time")
	private String exposureTime;

	@Column(name = "f_number")
	private String fNumber;

	@Column(name = "focal_length")
	private String focalLength;

	@Column(name = "lens_model")
	private String lensModel;

	@Column(name = "white_balance")
	private String whiteBalance;

	@Column(name = "exposure_mode")
	private String exposureMode;

	@Column(name = "exposure_program")
	private String exposureProgram;

	@Column(name = "metering_mode")
	private String meteringMode;

	@Column(name = "exif_date")
	private LocalDateTime exifDate;

	@Column(name = "exif_json")
	private String exifJson;
}