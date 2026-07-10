package br.com.jorgemelo.nimbusfilemanager.security.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jorgemelo.nimbusfilemanager.security.domain.model.UserAccessLog;

public interface UserAccessLogRepository extends JpaRepository<UserAccessLog, Long> {

	List<UserAccessLog> findByUsernameIgnoreCaseOrderByCreatedAtDesc(String username);
}