package br.com.jorgemelo.nimbusfilemanager.security.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import br.com.jorgemelo.nimbusfilemanager.security.application.AppUserAccountService;
import br.com.jorgemelo.nimbusfilemanager.security.application.QrCodeService;
import br.com.jorgemelo.nimbusfilemanager.security.application.TwoFactorEnrollmentService;
import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.AppUserRepository;

/**
 * Account page rendering (with/without a 2FA secret), password-change error
 * paths and the two-factor guards not covered by {@code WebControllersTest}.
 */
class AccountWebControllerExtraTest {

	private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
	private final AppUserAccountService appUserAccountService = mock(AppUserAccountService.class);
	private final QrCodeService qrCodeService = mock(QrCodeService.class);
	private final TwoFactorEnrollmentService twoFactorEnrollmentService = mock(TwoFactorEnrollmentService.class);

	private final AccountWebController controller = new AccountWebController(appUserRepository, appUserAccountService,
			qrCodeService, twoFactorEnrollmentService);

	private final Authentication auth = new TestingAuthenticationToken("bob", "x");

	@Test
	void accountRendersWithoutOtpUriWhenNoSecret() {
		when(appUserRepository.findByUsername("bob"))
				.thenReturn(Optional.of(AppUser.builder().username("bob").build()));

		ExtendedModelMap model = new ExtendedModelMap();

		Assertions.assertThat(controller.account(auth, model)).isEqualTo("app/account");
		Assertions.assertThat(model.get("otpUri")).isNull();
		Assertions.assertThat(model.get("user")).isNotNull();
	}

	@Test
	void accountBuildsOtpUriWhenSecretPresent() {
		when(appUserRepository.findByUsername("bob"))
				.thenReturn(Optional.of(AppUser.builder().username("bob").twoFactorSecret("SECRET").build()));

		ExtendedModelMap model = new ExtendedModelMap();

		controller.account(auth, model);

		Assertions.assertThat(model.get("otpUri").toString()).startsWith("otpauth://totp/");
	}

	@Test
	void changePasswordRejectsMismatchedConfirmation() {
		RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

		String view = controller.changePassword("current", "new1", "new2", auth, redirect);

		Assertions.assertThat(view).isEqualTo("redirect:/app/account");
		Assertions.assertThat(redirect.getFlashAttributes()).containsKey("passwordError");
	}

	@Test
	void changePasswordReportsServiceError() {
		doThrow(new IllegalArgumentException("Senha atual inválida.")).when(appUserAccountService)
				.changePassword(eq("bob"), any(), any());

		RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

		controller.changePassword("wrong", "newSecret", "newSecret", auth, redirect);

		Assertions.assertThat(redirect.getFlashAttributes()).extractingByKey("passwordError")
				.isEqualTo("Senha atual inválida.");
	}

	@Test
	void forcedFirstLoginPasswordChangeRedirectsIntoTheApp() {
		when(appUserRepository.findByUsername("bob"))
				.thenReturn(Optional.of(AppUser.builder().username("bob").passwordChangeRequired(true).build()));

		RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

		String view = controller.changePassword("current", "newSecret", "newSecret", auth, redirect);

		Assertions.assertThat(view).isEqualTo("redirect:/app");
		Assertions.assertThat(redirect.getFlashAttributes()).extractingByKey("passwordChanged").isEqualTo(true);

		// forced change resets without the current password (no browser-autofill
		// collision)
		verify(appUserAccountService).resetRequiredPassword("bob", "newSecret");
	}

	@Test
	void voluntaryPasswordChangeStaysOnAccountPage() {
		when(appUserRepository.findByUsername("bob"))
				.thenReturn(Optional.of(AppUser.builder().username("bob").passwordChangeRequired(false).build()));

		RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

		String view = controller.changePassword("current", "newSecret", "newSecret", auth, redirect);

		Assertions.assertThat(view).isEqualTo("redirect:/app/account");
		Assertions.assertThat(redirect.getFlashAttributes()).extractingByKey("passwordChanged").isEqualTo(true);
	}

	@Test
	void qrCodeIsNotFoundWhenUserHasNoSecret() {
		when(appUserRepository.findByUsername("bob"))
				.thenReturn(Optional.of(AppUser.builder().username("bob").build()));

		Assertions.assertThat(controller.twoFactorQrCode(auth).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void enableTwoFactorRejectsInvalidCode() {
		when(twoFactorEnrollmentService.enable("bob", "000000")).thenReturn(false);

		RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

		String view = controller.enableTwoFactor("000000", auth, redirect);

		Assertions.assertThat(view).isEqualTo("redirect:/app/account");
		Assertions.assertThat(redirect.getFlashAttributes()).containsKey("twoFactorError");
	}
}