package br.com.jorgemelo.nimbusfilemanager.security.application;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Turns the one 403 that is safe to honour - a CSRF-rejected
 * {@code POST /logout} - into an actual logout plus a redirect to the login
 * page, while leaving every other access denial as a normal 403.
 *
 * <p>
 * The usual trigger is a <b>stale CSRF token</b>: a browser tab left open
 * across an application restart still carries the previous instance's token and
 * session cookie, so clicking "Sair" (or the idle-timeout auto-submit) POSTs
 * {@code /logout} with a token the new instance rejects. The user's intent was
 * to log out anyway, and a CSRF-forged logout is at worst a nuisance, so we
 * honour it: invalidate the session, clear the security context and send the
 * user to {@code /login?logout} - never a raw 403 page.
 *
 * <p>
 * CSRF stays fully enforced for every other endpoint: non-logout denials are
 * delegated unchanged to {@link AccessDeniedHandlerImpl}, preserving the
 * existing 403 error page.
 */
@Component
public class LogoutAwareAccessDeniedHandler implements AccessDeniedHandler {

	private static final String LOGOUT_PATH = "/logout";

	private final AccessDeniedHandler defaultHandler = new AccessDeniedHandlerImpl();
	private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		if (LOGOUT_PATH.equals(request.getServletPath())) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			// Invalidates the session and clears the SecurityContext (no-op-safe when the
			// session is already gone after a restart), then lands on the login page.
			logoutHandler.logout(request, response, authentication);

			response.sendRedirect(request.getContextPath() + "/login?logout");

			return;
		}

		defaultHandler.handle(request, response, accessDeniedException);
	}
}