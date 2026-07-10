package br.com.jorgemelo.nimbusfilemanager.security.infrastructure.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.jorgemelo.nimbusfilemanager.security.application.AppUserAccountService;
import br.com.jorgemelo.nimbusfilemanager.security.application.QrCodeService;
import br.com.jorgemelo.nimbusfilemanager.security.application.TwoFactorEnrollmentService;
import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.AppUserRepository;
import br.com.jorgemelo.nimbusfilemanager.shared.i18n.LocalizedComponent;

@Controller
public class AccountWebController extends LocalizedComponent {

	private static final String ISSUER = "Nimbus File Manager";
	private static final String REDIRECT_ACCOUNT = "redirect:/app/account";

	private final AppUserRepository appUserRepository;
	private final AppUserAccountService appUserAccountService;
	private final QrCodeService qrCodeService;
	private final TwoFactorEnrollmentService twoFactorEnrollmentService;

	public AccountWebController(AppUserRepository appUserRepository, AppUserAccountService appUserAccountService,
			QrCodeService qrCodeService, TwoFactorEnrollmentService twoFactorEnrollmentService) {
		this.appUserRepository = appUserRepository;
		this.appUserAccountService = appUserAccountService;
		this.qrCodeService = qrCodeService;
		this.twoFactorEnrollmentService = twoFactorEnrollmentService;
	}

	@GetMapping("/app/account")
	public String account(Authentication authentication, Model model) {
		AppUser user = currentUser(authentication);

		model.addAttribute("user", user);
		model.addAttribute("otpUri", otpUri(user));

		return "app/account";
	}

	@PostMapping("/app/account/password")
	public String changePassword(@RequestParam(required = false) String currentPassword,
			@RequestParam String newPassword, @RequestParam String confirmPassword, Authentication authentication,
			RedirectAttributes redirectAttributes) {
		if (!newPassword.equals(confirmPassword)) {
			redirectAttributes.addFlashAttribute("passwordError", message("backend.passwordsMismatch"));

			return REDIRECT_ACCOUNT;
		}

		boolean changeWasRequired = appUserRepository.findByUsername(authentication.getName())
				.map(user -> Boolean.TRUE.equals(user.getPasswordChangeRequired())).orElse(false);

		try {
			// Forced first-login change: the user just authenticated with the default, so
			// we
			// don't ask for the current password again (it also breaks browser autofill).
			if (changeWasRequired) {
				appUserAccountService.resetRequiredPassword(authentication.getName(), newPassword);
			} else {
				appUserAccountService.changePassword(authentication.getName(), currentPassword, newPassword);
			}

			redirectAttributes.addFlashAttribute("passwordChanged", true);
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("passwordError", exception.getMessage());

			return REDIRECT_ACCOUNT;
		}

		// After the forced first-login password change, move the user forward into
		// the app (the dashboard routes first-run users to onboarding) instead of
		// stranding them on the account page where they'd have to log out and back in.
		if (changeWasRequired) {
			return "redirect:/app";
		}

		return REDIRECT_ACCOUNT;
	}

	@GetMapping(value = "/app/account/2fa/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> twoFactorQrCode(Authentication authentication) {
		AppUser user = currentUser(authentication);

		String otpUri = otpUri(user);

		if (otpUri == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA, "no-cache")
				.contentType(MediaType.IMAGE_PNG).body(qrCodeService.png(otpUri));
	}

	@PostMapping("/app/account/2fa/prepare")
	public String prepareTwoFactor(Authentication authentication) {
		twoFactorEnrollmentService.prepareSecret(authentication.getName());

		return REDIRECT_ACCOUNT;
	}

	@PostMapping("/app/account/2fa/enable")
	public String enableTwoFactor(@RequestParam String code, Authentication authentication,
			RedirectAttributes redirectAttributes) {
		if (!twoFactorEnrollmentService.enable(authentication.getName(), code)) {
			redirectAttributes.addFlashAttribute("twoFactorError", true);

			return REDIRECT_ACCOUNT;
		}

		redirectAttributes.addFlashAttribute("twoFactorEnabled", true);

		return REDIRECT_ACCOUNT;
	}

	@PostMapping("/app/account/2fa/disable")
	public String disableTwoFactor(Authentication authentication, RedirectAttributes redirectAttributes) {
		twoFactorEnrollmentService.disable(authentication.getName());

		redirectAttributes.addFlashAttribute("twoFactorDisabled", true);

		return REDIRECT_ACCOUNT;
	}

	private AppUser currentUser(Authentication authentication) {
		return appUserRepository.findByUsername(authentication.getName()).orElseThrow(
				() -> new IllegalStateException("Authenticated user not found: " + authentication.getName()));
	}

	private String otpUri(AppUser user) {
		if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().isBlank()) {
			return null;
		}

		String label = encode(ISSUER + ":" + user.getUsername());

		return "otpauth://totp/" + label + "?secret=" + user.getTwoFactorSecret() + "&issuer=" + encode(ISSUER);
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}
}