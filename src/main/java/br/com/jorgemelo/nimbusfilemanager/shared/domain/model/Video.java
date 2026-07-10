package br.com.jorgemelo.nimbusfilemanager.shared.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "video")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Video {

	@Id
	@Column(name = "catalog_file_id")
	@EqualsAndHashCode.Include
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catalog_file_id")
	@ToString.Exclude
	private CatalogFile catalogFile;

	@Column(name = "container")
	private String container;

	@Column(name = "video_codec")
	private String videoCodec;

	@Column(name = "audio_codec")
	private String audioCodec;

	@Column(name = "video_profile")
	private String videoProfile;

	@Column(name = "fps")
	private Double fps;

	@Column(name = "video_bitrate")
	private Long videoBitrate;

	@Column(name = "total_bitrate")
	private Long totalBitrate;

	@Column(name = "duration_seconds")
	private Double durationSeconds;

	@Column(name = "hdr")
	private Boolean hdr;

	@Column(name = "pixel_format")
	private String pixelFormat;

	@Column(name = "color_space")
	private String colorSpace;

	@Column(name = "color_transfer")
	private String colorTransfer;

	@Column(name = "color_primaries")
	private String colorPrimaries;

	@Column(name = "bit_depth")
	private Integer bitDepth;

	@Column(name = "audio_sample_rate")
	private Integer audioSampleRate;

	@Column(name = "audio_channels")
	private Integer audioChannels;

	@Column(name = "audio_channel_layout")
	private String audioChannelLayout;

	@Column(name = "mediainfo_json")
	private String mediaInfoJson;

	@PrePersist
	void prePersist() {
		if (hdr == null) {
			hdr = false;
		}
	}
}