package br.com.jorgemelo.nimbusfilemanager.security.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import br.com.jorgemelo.nimbusfilemanager.security.application.constants.SecurityConstants;
import br.com.jorgemelo.nimbusfilemanager.security.domain.model.UserAccessLog;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.UserAccessLogRepository;

class AppLogoutSuccessHandlerTest {

	@Test
	void onLogoutSuccessShouldRecordManualLogoutAndRedirectWhenNoReasonGiven() throws Exception {
		UserAccessLogRepository repository = mock(UserAccessLogRepository.class);

		UserAccessLogService userAccessLogService = new UserAccessLogService(repository);

		AppLogoutSuccessHandler handler = new AppLogoutSuccessHandler(userAccessLogService);

		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		Authentication authentication = new TestingAuthenticationToken("admin", "n/a");

		when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		handler.onLogoutSuccess(request, response, authentication);

		ArgumentCaptor<UserAccessLog> captor = ArgumentCaptor.forClass(UserAccessLog.class);

		verify(repository).save(captor.capture());

		Assertions.assertThat(captor.getValue().getEventType()).isEqualTo(SecurityConstants.LOGOUT);
		Assertions.assertThat(response.getRedirectedUrl()).isEqualTo("/login?logout");
	}

	@Test
	void onLogoutSuccessShouldRecordInactivityLogoutAndRedirectWhenReasonIsInactivity() throws Exception {
		UserAccessLogRepository repository = mock(UserAccessLogRepository.class);

		UserAccessLogService userAccessLogService = new UserAccessLogService(repository);

		AppLogoutSuccessHandler handler = new AppLogoutSuccessHandler(userAccessLogService);

		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		Authentication authentication = new TestingAuthenticationToken("admin", "n/a");

		request.setParameter("reason", "inactivity");

		when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		handler.onLogoutSuccess(request, response, authentication);

		ArgumentCaptor<UserAccessLog> captor = ArgumentCaptor.forClass(UserAccessLog.class);

		verify(repository).save(captor.capture());

		Assertions.assertThat(captor.getValue().getEventType()).isEqualTo(SecurityConstants.LOGOUT_INACTIVITY);
		Assertions.assertThat(response.getRedirectedUrl()).isEqualTo("/login?idle");
	}

	@Test
	void onLogoutSuccessShouldSkipLoggingWhenAuthenticationIsNull() throws Exception {
		UserAccessLogRepository repository = mock(UserAccessLogRepository.class);

		UserAccessLogService userAccessLogService = new UserAccessLogService(repository);

		AppLogoutSuccessHandler handler = new AppLogoutSuccessHandler(userAccessLogService);

		MockHttpServletRequest request = new MockHttpServletRequest();

		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.onLogoutSuccess(request, response, null);

		verify(repository, never()).save(any());

		Assertions.assertThat(response.getRedirectedUrl()).isEqualTo("/login?logout");
	}
}