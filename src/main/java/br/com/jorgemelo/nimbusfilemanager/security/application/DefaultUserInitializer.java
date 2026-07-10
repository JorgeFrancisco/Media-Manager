package br.com.jorgemelo.nimbusfilemanager.security.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.security.domain.enums.Role;
import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.AppUserRepository;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.NimbusFileManagerProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DefaultUserInitializer implements ApplicationRunner {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final String username;
	private final String password;

	public DefaultUserInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
			NimbusFileManagerProperties properties) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.username = properties.security().defaultUsername();
		this.password = properties.security().defaultPassword();
	}

	@Override
	public void run(ApplicationArguments args) {
		if (appUserRepository.count() > 0) {
			markLegacyDefaultPassword();
			return;
		}

		// Seeded with the configured default password
		// (nimbus-file-manager.security.default-password) -
		// a known/published value, so a change is always required on first login.
		appUserRepository.save(AppUser.builder().username(username).passwordHash(passwordEncoder.encode(password))
				.displayName("Administrator").role(Role.ADMIN).enabled(true).twoFactorEnabled(false)
				.passwordChangeRequired(true).build());

		log.warn("Default application user created. username={}. Change the default password immediately.", username);
	}

	private void markLegacyDefaultPassword() {
		appUserRepository.findByUsernameIgnoreCase(username)
				.filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
				.filter(user -> !Boolean.TRUE.equals(user.getPasswordChangeRequired())).ifPresent(user -> {
					user.setPasswordChangeRequired(true);
					appUserRepository.save(user);
					log.warn("Default password is still in use. Password change is required for username={}.",
							username);
				});
	}
}