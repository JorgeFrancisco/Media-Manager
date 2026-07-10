package br.com.jorgemelo.nimbusfilemanager.security.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.AppUserRepository;

/**
 * Owns the 2FA enrollment state transitions - generating the shared TOTP
 * secret, confirming it with a first valid code and turning 2FA back off - so
 * {@code AccountWebController} only orchestrates presentation (flash attributes
 * and redirects). Keeping the domain mutations here also makes them
 * transactional and reusable outside the web layer.
 */
@Service
public class TwoFactorEnrollmentService {

	private final AppUserRepository appUserRepository;
	private final TwoFactorService twoFactorService;

	public TwoFactorEnrollmentService(AppUserRepository appUserRepository, TwoFactorService twoFactorService) {
		this.appUserRepository = appUserRepository;
		this.twoFactorService = twoFactorService;
	}

	@Transactional
	public void prepareSecret(String username) {
		AppUser user = requireUser(username);

		if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().isBlank()) {
			user.setTwoFactorSecret(twoFactorService.newSecret());

			appUserRepository.save(user);
		}
	}

	@Transactional
	public boolean enable(String username, String code) {
		AppUser user = requireUser(username);

		if (!twoFactorService.verify(user.getTwoFactorSecret(), code)) {
			return false;
		}

		user.setTwoFactorEnabled(true);

		appUserRepository.save(user);

		return true;
	}

	@Transactional
	public void disable(String username) {
		AppUser user = requireUser(username);

		user.setTwoFactorEnabled(false);

		appUserRepository.save(user);
	}

	private AppUser requireUser(String username) {
		return appUserRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
	}
}