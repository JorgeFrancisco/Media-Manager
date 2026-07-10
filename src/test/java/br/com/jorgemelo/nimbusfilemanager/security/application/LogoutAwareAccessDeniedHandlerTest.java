package br.com.jorgemelo.nimbusfilemanager.security.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.http.HttpServletResponse;

class LogoutAwareAccessDeniedHandlerTest {

	private final LogoutAwareAccessDeniedHandler handler = new LogoutAwareAccessDeniedHandler();

	@Test
	void logoutDenialBecomesGracefulLogoutAndRedirect() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setServletPath("/logout");

		MockHttpSession session = new MockHttpSession();

		request.setSession(session);

		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.handle(request, response, new AccessDeniedException("Invalid CSRF token"));

		assertThat(session.isInvalid()).as("session invalidated").isTrue();
		assertThat(response.getRedirectedUrl()).isEqualTo("/login?logout");
	}

	@Test
	void logoutDenialWithoutSessionStillRedirects() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setServletPath("/logout");

		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.handle(request, response, new AccessDeniedException("Missing CSRF token"));

		assertThat(response.getRedirectedUrl()).isEqualTo("/login?logout");
	}

	@Test
	void nonLogoutDenialKeepsDefault403() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.setServletPath("/app/users");

		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.handle(request, response, new AccessDeniedException("Access is denied"));

		assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
		assertThat(response.getRedirectedUrl()).isNull();
	}
}