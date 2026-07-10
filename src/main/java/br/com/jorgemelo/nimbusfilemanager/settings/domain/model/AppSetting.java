package br.com.jorgemelo.nimbusfilemanager.settings.domain.model;

import java.time.LocalDateTime;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.ClockHolder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_setting")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AppSetting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name = "setting_key", nullable = false, unique = true, length = 150)
	private String settingKey;

	@Column(name = "setting_value")
	private String settingValue;

	@Column(name = "value_type", nullable = false, length = 30)
	private String valueType;

	@Column
	private String description;

	@Column(nullable = false)
	private Boolean editable;

	@Column(name = "created_by_username", nullable = false, length = 100)
	private String createdByUsername;

	@Column(name = "updated_by_username", length = 100)
	private String updatedByUsername;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now(ClockHolder.clock());

		if (editable == null) {
			editable = true;
		}

		if (createdByUsername == null || createdByUsername.isBlank()) {
			createdByUsername = "system";
		}

		if (createdAt == null) {
			createdAt = now;
		}

		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now(ClockHolder.clock());
	}
}