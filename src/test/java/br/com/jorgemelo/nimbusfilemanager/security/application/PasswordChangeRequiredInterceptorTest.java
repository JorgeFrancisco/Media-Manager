package br.com.jorgemelo.nimbusfilemanager.security.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.AppUserRepository;

class PasswordChangeRequiredInterceptorTest {

	private final AppUserRepository repository = mock(AppUserRepository.class);
	private final PasswordChangeRequiredInterceptor interceptor = new PasswordChangeRequiredInterceptor(repository);

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldRedirectAuthenticatedUserWhosePasswordMustChange() throws Exception {
		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("admin@example.com", "pw", "ROLE_ADMIN"));

		when(repository.findByUsernameIgnoreCase("admin@example.com"))
				.thenReturn(Optional.of(AppUser.builder().passwordChangeRequired(true).build()));

		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		Assertions.assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
		Assertions.assertThat(response.getRedirectedUrl()).isEqualTo("/app/account?passwordChangeRequired=true");
	}

	@Test
	void shouldRejectApiForUserWhosePasswordMustChange() throws Exception {
		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("admin@example.com", "pw", "ROLE_ADMIN"));

		when(repository.findByUsernameIgnoreCase("admin@example.com"))
				.thenReturn(Optional.of(AppUser.builder().passwordChangeRequired(true).build()));

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/organization/execute");

		MockHttpServletResponse response = new MockHttpServletResponse();

		Assertions.assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
		Assertions.assertThat(response.getStatus()).isEqualTo(403);
	}

	@Test
	void shouldAllowAnonymousAndUsersWhoAlreadyChangedPassword() throws Exception {
		Assertions.assertThat(
				interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()))
				.isTrue();

		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("user@example.com", "pw", "ROLE_USER"));

		when(repository.findByUsernameIgnoreCase("user@example.com"))
				.thenReturn(Optional.of(AppUser.builder().passwordChangeRequired(false).build()));

		Assertions.assertThat(
				interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()))
				.isTrue();

		verify(repository).findByUsernameIgnoreCase("user@example.com");
	}
}