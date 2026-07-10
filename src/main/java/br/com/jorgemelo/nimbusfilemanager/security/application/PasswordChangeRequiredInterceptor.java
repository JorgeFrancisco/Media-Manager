package br.com.jorgemelo.nimbusfilemanager.security.application;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PasswordChangeRequiredInterceptor implements HandlerInterceptor {

	private final AppUserRepository appUserRepository;

	public PasswordChangeRequiredInterceptor(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || authentication instanceof AnonymousAuthenticationToken
				|| !authentication.isAuthenticated()) {
			return true;
		}

		boolean passwordChangeRequired = appUserRepository.findByUsernameIgnoreCase(authentication.getName())
				.map(user -> Boolean.TRUE.equals(user.getPasswordChangeRequired())).orElse(false);

		if (!passwordChangeRequired) {
			return true;
		}

		if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Change the default password before using the API.");

			return false;
		}

		response.sendRedirect(request.getContextPath() + "/app/account?passwordChangeRequired=true");

		return false;
	}
}