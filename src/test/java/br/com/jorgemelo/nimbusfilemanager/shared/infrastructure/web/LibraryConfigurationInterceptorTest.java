package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.settings.application.AppSettingService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class LibraryConfigurationInterceptorTest {

	private final AppSettingService settings = mock(AppSettingService.class);
	private final HttpServletRequest request = mock(HttpServletRequest.class);
	private final HttpServletResponse response = mock(HttpServletResponse.class);
	private final LibraryConfigurationInterceptor interceptor = new LibraryConfigurationInterceptor(settings);

	@Test
	void redirectsAppPagesToOnboardingWhenLibraryIsNotConfigured() throws Exception {
		when(settings.stringValue(SettingsConstants.WATCH_FOLDER, "")).thenReturn("");
		when(request.getRequestURI()).thenReturn("/app/files");
		when(request.getContextPath()).thenReturn("");

		Assertions.assertThat(interceptor.preHandle(request, response, new Object())).isFalse();

		verify(response).sendRedirect("/app/onboarding");
	}

	@Test
	void rejectsApiCallsWhenLibraryIsNotConfigured() throws Exception {
		when(settings.stringValue(SettingsConstants.WATCH_FOLDER, "")).thenReturn(" ");
		when(request.getRequestURI()).thenReturn("/api/organization/preview");
		when(request.getContextPath()).thenReturn("");

		Assertions.assertThat(interceptor.preHandle(request, response, new Object())).isFalse();

		verify(response).sendError(HttpServletResponse.SC_CONFLICT,
				"Configure a pasta monitorada antes de usar as funcionalidades da aplicação.");
	}

	@Test
	void allowsRequestsAfterLibraryIsConfigured() throws Exception {
		when(settings.stringValue(SettingsConstants.WATCH_FOLDER, "")).thenReturn("C:/media");

		Assertions.assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
	}
}