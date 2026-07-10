package br.com.jorgemelo.nimbusfilemanager.security.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.AppUserRepository;

class TwoFactorEnrollmentServiceTest {

	private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
	private final TwoFactorService twoFactorService = mock(TwoFactorService.class);
	private final TwoFactorEnrollmentService service = new TwoFactorEnrollmentService(appUserRepository,
			twoFactorService);

	private AppUser user(String secret, boolean enabled) {
		AppUser user = AppUser.builder().username("admin").twoFactorSecret(secret).twoFactorEnabled(enabled).build();

		when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));

		return user;
	}

	@Test
	void prepareSecretGeneratesAndSavesWhenAbsent() {
		AppUser user = user(null, false);

		when(twoFactorService.newSecret()).thenReturn("SECRET");

		service.prepareSecret("admin");

		Assertions.assertThat(user.getTwoFactorSecret()).isEqualTo("SECRET");

		verify(appUserRepository).save(user);
	}

	@Test
	void prepareSecretIsNoOpWhenSecretAlreadyPresent() {
		AppUser user = user("EXISTING", false);

		service.prepareSecret("admin");

		Assertions.assertThat(user.getTwoFactorSecret()).isEqualTo("EXISTING");

		verify(twoFactorService, never()).newSecret();
		verify(appUserRepository, never()).save(any());
	}

	@Test
	void enableReturnsFalseAndDoesNotSaveWhenCodeIsInvalid() {
		AppUser user = user("SECRET", false);

		when(twoFactorService.verify("SECRET", "000000")).thenReturn(false);

		boolean result = service.enable("admin", "000000");

		Assertions.assertThat(result).isFalse();
		Assertions.assertThat(user.getTwoFactorEnabled()).isFalse();

		verify(appUserRepository, never()).save(any());
	}

	@Test
	void enableTurnsOnTwoFactorAndSavesWhenCodeIsValid() {
		AppUser user = user("SECRET", false);

		when(twoFactorService.verify("SECRET", "123456")).thenReturn(true);

		boolean result = service.enable("admin", "123456");

		Assertions.assertThat(result).isTrue();
		Assertions.assertThat(user.getTwoFactorEnabled()).isTrue();

		verify(appUserRepository).save(user);
	}

	@Test
	void disableClearsFlagAndSaves() {
		AppUser user = user("SECRET", true);

		service.disable("admin");

		Assertions.assertThat(user.getTwoFactorEnabled()).isFalse();

		verify(appUserRepository).save(user);
	}

	@Test
	void requireUserRaisesWhenAuthenticatedUserIsMissing() {
		when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

		Assertions.assertThatThrownBy(() -> service.disable("ghost")).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ghost");
	}
}