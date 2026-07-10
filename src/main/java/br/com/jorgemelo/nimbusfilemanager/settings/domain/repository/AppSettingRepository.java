package br.com.jorgemelo.nimbusfilemanager.settings.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.jorgemelo.nimbusfilemanager.settings.domain.model.AppSetting;

public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {

	Optional<AppSetting> findBySettingKey(String settingKey);

	List<AppSetting> findAllByOrderBySettingKeyAsc();
}