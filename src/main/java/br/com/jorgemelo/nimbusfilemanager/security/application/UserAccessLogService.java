package br.com.jorgemelo.nimbusfilemanager.security.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jorgemelo.nimbusfilemanager.security.domain.model.UserAccessLog;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.UserAccessLogRepository;

@Service
public class UserAccessLogService {

	private final UserAccessLogRepository userAccessLogRepository;

	public UserAccessLogService(UserAccessLogRepository userAccessLogRepository) {
		this.userAccessLogRepository = userAccessLogRepository;
	}

	@Transactional
	public void recordAccess(String username, String eventType, String status, String ipAddress, String userAgent,
			String message) {
		userAccessLogRepository.save(UserAccessLog.builder().username(username).eventType(eventType).status(status)
				.ipAddress(ipAddress).userAgent(userAgent).message(message).build());
	}

	@Transactional(readOnly = true)
	public List<UserAccessLog> findByEmail(String email) {
		if (email == null || email.isBlank()) {
			return List.of();
		}

		return userAccessLogRepository.findByUsernameIgnoreCaseOrderByCreatedAtDesc(email.trim());
	}
}