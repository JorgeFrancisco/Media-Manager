package br.com.jorgemelo.nimbusfilemanager.security.infrastructure.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.NimbusFileManagerProperties;

/**
 * Resolves whether Google login should be offered on the auth screens and
 * exposes that decision to the view. Keeping this concern out of the auth
 * controller separates OAuth availability resolution from request handling.
 */
@Component
public class GoogleLoginStatus {

	private final boolean enabled;
	private final boolean available;

	public GoogleLoginStatus(NimbusFileManagerProperties properties,
			@Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
			@Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret) {
		this.enabled = properties.security().googleLoginEnabled();
		this.available = enabled && configured(googleClientId, googleClientSecret);
	}

	void addAttributes(Model model) {
		model.addAttribute("googleLoginEnabled", enabled);
		model.addAttribute("googleLoginAvailable", available);
	}

	private boolean configured(String googleClientId, String googleClientSecret) {
		return googleClientId != null && !googleClientId.isBlank() && googleClientSecret != null
				&& !googleClientSecret.isBlank();
	}
}