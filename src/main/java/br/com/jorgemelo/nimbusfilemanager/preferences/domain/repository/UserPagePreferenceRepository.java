package br.com.jorgemelo.nimbusfilemanager.preferences.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jorgemelo.nimbusfilemanager.preferences.domain.model.UserPagePreference;

public interface UserPagePreferenceRepository extends JpaRepository<UserPagePreference, Long> {

	List<UserPagePreference> findByUserIdAndPageKey(Long userId, String pageKey);

	Optional<UserPagePreference> findByUserIdAndPageKeyAndPreferenceKey(Long userId, String pageKey,
			String preferenceKey);
}