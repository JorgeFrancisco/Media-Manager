package br.com.jorgemelo.nimbusfilemanager.security.domain.model;

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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_access_log")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserAccessLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(length = 100)
	private String username;

	@Column(name = "event_type", nullable = false, length = 50)
	private String eventType;

	@Column(nullable = false, length = 50)
	private String status;

	@Column(name = "ip_address", length = 100)
	private String ipAddress;

	@Column(name = "user_agent")
	private String userAgent;

	@Column
	private String message;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now(ClockHolder.clock());
		}
	}
}