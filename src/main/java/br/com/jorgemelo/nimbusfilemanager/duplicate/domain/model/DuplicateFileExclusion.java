package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.ClockHolder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single media file the user chose to keep in the catalog but hide from
 * duplicate comparison (both the exact and the similar tabs). Referenced by the
 * stable {@code public_id}; the FK cascade drops the row if the file is ever
 * hard-deleted from the catalog.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "duplicate_file_exclusion")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DuplicateFileExclusion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name = "public_id", nullable = false, unique = true)
	private UUID publicId;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now(ClockHolder.clock());
		}
	}
}