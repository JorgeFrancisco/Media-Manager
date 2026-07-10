package br.com.jorgemelo.nimbusfilemanager.security.infrastructure.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.jorgemelo.nimbusfilemanager.notification.application.EmailService;
import br.com.jorgemelo.nimbusfilemanager.security.application.AppUserAccountService;
import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;
import br.com.jorgemelo.nimbusfilemanager.shared.i18n.LocalizedComponent;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Self-service sign-up with an email-confirmation gate. Actually sending the
 * confirmation email is delegated to {@link EmailService}, which falls back to
 * just logging the link when no email provider is configured (the default out
 * of the box) - same stand-in used elsewhere in this app for the seeded admin
 * password (see DefaultUserInitializer).
 */
@Controller
public class RegistrationWebController extends LocalizedComponent {

	private static final String VIEW_REGISTER = "auth/register";

	private final AppUserAccountService appUserAccountService;
	private final EmailService emailService;

	public RegistrationWebController(AppUserAccountService appUserAccountService, EmailService emailService) {
		this.appUserAccountService = appUserAccountService;
		this.emailService = emailService;
	}

	@GetMapping("/register")
	public String register() {
		return VIEW_REGISTER;
	}

	@PostMapping("/register")
	public String register(@RequestParam String email, @RequestParam String displayName, @RequestParam String password,
			@RequestParam String confirmPassword, HttpServletRequest request, Model model,
			RedirectAttributes redirectAttributes) {
		if (!password.equals(confirmPassword)) {
			model.addAttribute("registerError", message("backend.passwordsMismatch"));
			model.addAttribute("email", email);
			model.addAttribute("displayName", displayName);

			return VIEW_REGISTER;
		}

		try {
			AppUser user = appUserAccountService.register(email, displayName, password);

			String confirmationLink = confirmationLink(request, user.getConfirmationToken());

			emailService.sendConfirmationEmail(user.getUsername(), confirmationLink);

			redirectAttributes.addFlashAttribute("registered", true);

			return "redirect:/login";
		} catch (IllegalArgumentException exception) {
			model.addAttribute("registerError", exception.getMessage());
			model.addAttribute("email", email);
			model.addAttribute("displayName", displayName);

			return VIEW_REGISTER;
		}
	}

	@GetMapping("/confirm")
	public String confirm(@RequestParam String token, RedirectAttributes redirectAttributes) {
		try {
			appUserAccountService.confirmRegistration(token);

			redirectAttributes.addFlashAttribute("confirmed", true);
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("confirmError", exception.getMessage());
		}

		return "redirect:/login";
	}

	private String confirmationLink(HttpServletRequest request, String token) {
		String scheme = request.getScheme();

		String host = request.getServerName();

		int port = request.getServerPort();

		boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);

		String authority = defaultPort ? host : host + ":" + port;

		return scheme + "://" + authority + "/confirm?token=" + token;
	}
}