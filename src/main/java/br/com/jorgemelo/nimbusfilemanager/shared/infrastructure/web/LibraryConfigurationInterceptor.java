package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import br.com.jorgemelo.nimbusfilemanager.settings.application.AppSettingService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;
import br.com.jorgemelo.nimbusfilemanager.shared.i18n.LocalizedComponent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LibraryConfigurationInterceptor extends LocalizedComponent implements HandlerInterceptor {

	private final AppSettingService appSettingService;

	public LibraryConfigurationInterceptor(AppSettingService appSettingService) {
		this.appSettingService = appSettingService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (appSettingService.stringValue(SettingsConstants.WATCH_FOLDER, "").isBlank()) {
			if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
				response.sendError(HttpServletResponse.SC_CONFLICT, message("backend.library.notConfigured"));
				return false;
			}

			response.sendRedirect(request.getContextPath() + "/app/onboarding");

			return false;
		}

		return true;
	}
}