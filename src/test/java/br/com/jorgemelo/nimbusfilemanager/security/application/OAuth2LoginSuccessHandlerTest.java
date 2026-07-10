package br.com.jorgemelo.nimbusfilemanager.security.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import br.com.jorgemelo.nimbusfilemanager.security.application.constants.SecurityConstants;
import br.com.jorgemelo.nimbusfilemanager.security.domain.model.AppUser;

class OAuth2LoginSuccessHandlerTest {

	private final AppUserAccountService accountService = mock(AppUserAccountService.class);
	private final AppUserDetailsService userDetailsService = mock(AppUserDetailsService.class);
	private final UserAccessLogService accessLogService = mock(UserAccessLogService.class);
	private final AccountLockService accountLockService = mock(AccountLockService.class);
	private final OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(accountService, userDetailsService,
			accessLogService, accountLockService);

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void googleLoginShouldRequireTotpWhenLocalAccountHasTwoFactorEnabled() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		AppUser user = AppUser.builder().username("admin@example.com").twoFactorEnabled(true).build();

		when(accountService.upsertOAuthUser("Admin@Example.com", "Admin")).thenReturn(user);

		handler.onAuthenticationSuccess(request, response, oauth("Admin@Example.com", "Admin"));

		Assertions.assertThat(response.getRedirectedUrl()).isEqualTo("/login/2fa");
		Assertions.assertThat(request.getSession().getAttribute(SecurityConstants.PENDING_USERNAME))
				.isEqualTo("admin@example.com");
		Assertions.assertThat(
				request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY))
				.isNull();
		Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

		verify(userDetailsService, never()).loadUserByUsername(any());
		verify(accessLogService).recordAccess(eq("admin@example.com"), eq(SecurityConstants.LOGIN_2FA_REQUIRED),
				eq("SUCCESS"), eq("127.0.0.1"), isNull(), any());
		verify(accountLockService, never()).registerSuccess(any());
	}

	@Test
	void googleLoginShouldCreateLocalAuthenticationWhenTwoFactorIsDisabled() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		AppUser user = AppUser.builder().username("user@example.com").twoFactorEnabled(false).build();

		User userDetails = new User("user@example.com", "", List.of(new SimpleGrantedAuthority("ROLE_USER")));

		when(accountService.upsertOAuthUser("User@Example.com", "User")).thenReturn(user);
		when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);

		handler.onAuthenticationSuccess(request, response, oauth("User@Example.com", "User"));

		Assertions.assertThat(response.getRedirectedUrl()).isEqualTo("/app");
		Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
				.isEqualTo("user@example.com");
		Assertions.assertThat(
				request.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY))
				.isNotNull();

		verify(accessLogService).recordAccess(eq("user@example.com"), eq(SecurityConstants.LOGIN_SUCCESS),
				eq("SUCCESS"), eq("127.0.0.1"), isNull(), any());
		verify(accountLockService).registerSuccess("user@example.com");
	}

	@Test
	void googleLoginShouldRejectProviderResponseWithoutEmail() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		OAuth2AuthenticationToken token = oauth(null, "User");

		Assertions.assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, token))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("e-mail");

		verify(accountService, never()).upsertOAuthUser(any(), any());
	}

	private OAuth2AuthenticationToken oauth(String email, String name) {
		Map<String, Object> attributes = email == null ? Map.of("name", name) : Map.of("email", email, "name", name);

		var principal = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "name");

		return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");
	}
}