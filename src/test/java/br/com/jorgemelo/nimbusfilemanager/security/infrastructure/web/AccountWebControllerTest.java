package br.com.jorgemelo.nimbusfilemanager.security.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import br.com.jorgemelo.nimbusfilemanager.security.application.AppUserAccountService;
import br.com.jorgemelo.nimbusfilemanager.security.application.QrCodeService;
import br.com.jorgemelo.nimbusfilemanager.security.application.TwoFactorEnrollmentService;
import br.com.jorgemelo.nimbusfilemanager.security.domain.enums.Role;
import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.AppUserRepository;

class AccountWebControllerTest {

	@Test
	void accountShouldPrepareEnableAndDisableTwoFactor() {
		AppUserRepository appUserRepository = mock(AppUserRepository.class);
		AppUserAccountService appUserAccountService = mock(AppUserAccountService.class);
		QrCodeService qrCodeService = mock(QrCodeService.class);
		TwoFactorEnrollmentService twoFactorEnrollmentService = mock(TwoFactorEnrollmentService.class);
		AccountWebController controller = new AccountWebController(appUserRepository, appUserAccountService,
				qrCodeService, twoFactorEnrollmentService);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "password");
		RedirectAttributesModelMap enableRedirect = new RedirectAttributesModelMap();
		RedirectAttributesModelMap disableRedirect = new RedirectAttributesModelMap();

		when(twoFactorEnrollmentService.enable("admin", "123456")).thenReturn(true);

		Assertions.assertThat(controller.prepareTwoFactor(authentication)).isEqualTo("redirect:/app/account");
		Assertions.assertThat(controller.enableTwoFactor("123456", authentication, enableRedirect))
				.isEqualTo("redirect:/app/account");
		Assertions.assertThat(controller.disableTwoFactor(authentication, disableRedirect))
				.isEqualTo("redirect:/app/account");
		Assertions.assertThat(enableRedirect.getFlashAttributes()).containsKey("twoFactorEnabled");
		Assertions.assertThat(disableRedirect.getFlashAttributes()).containsKey("twoFactorDisabled");

		verify(twoFactorEnrollmentService).prepareSecret("admin");
		verify(twoFactorEnrollmentService).disable("admin");
	}

	@Test
	void accountShouldRenderTwoFactorQrCode() {
		AppUserRepository appUserRepository = mock(AppUserRepository.class);
		AppUserAccountService appUserAccountService = mock(AppUserAccountService.class);
		QrCodeService qrCodeService = mock(QrCodeService.class);
		TwoFactorEnrollmentService twoFactorEnrollmentService = mock(TwoFactorEnrollmentService.class);
		AccountWebController controller = new AccountWebController(appUserRepository, appUserAccountService,
				qrCodeService, twoFactorEnrollmentService);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", "password");
		AppUser user = user();
		byte[] png = new byte[] { 1, 2, 3 };

		user.setTwoFactorSecret("SECRET");
		when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
		when(qrCodeService.png(any())).thenReturn(png);

		var response = controller.twoFactorQrCode(authentication);

		Assertions.assertThat(response.getBody()).isSameAs(png);
		Assertions.assertThat(response.getHeaders().getContentType()).hasToString("image/png");
		verify(qrCodeService).png(any());
	}

	@Test
	void accountShouldChangePassword() {
		AppUserRepository appUserRepository = mock(AppUserRepository.class);
		AppUserAccountService appUserAccountService = mock(AppUserAccountService.class);
		QrCodeService qrCodeService = mock(QrCodeService.class);
		TwoFactorEnrollmentService twoFactorEnrollmentService = mock(TwoFactorEnrollmentService.class);
		AccountWebController controller = new AccountWebController(appUserRepository, appUserAccountService,
				qrCodeService, twoFactorEnrollmentService);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin@example.com", "password");

		String view = controller.changePassword("old", "newSecret", "newSecret", authentication,
				new RedirectAttributesModelMap());

		Assertions.assertThat(view).isEqualTo("redirect:/app/account");
		verify(appUserAccountService).changePassword("admin@example.com", "old", "newSecret");
	}

	private AppUser user() {
		return AppUser.builder().username("admin").passwordHash("hash").displayName("Admin").role(Role.ADMIN)
				.enabled(true).twoFactorEnabled(false).build();
	}
}