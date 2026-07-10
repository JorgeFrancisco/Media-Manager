package br.com.jorgemelo.nimbusfilemanager.duplicate.domain.model;

import java.time.LocalDateTime;

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
 * A whole folder (normalized absolute path) hidden from duplicate comparison,
 * recursively: every current and future file at or under this path is dropped
 * from both the exact and the similar duplicate views. The files stay fully
 * inventoried and visible everywhere else.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "duplicate_folder_exclusion")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DuplicateFolderExclusion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name = "folder_path", nullable = false, unique = true, length = 1024)
	private String folderPath;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now(ClockHolder.clock());
		}
	}
}