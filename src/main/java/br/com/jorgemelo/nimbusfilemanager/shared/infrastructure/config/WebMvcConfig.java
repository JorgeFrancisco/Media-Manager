package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.com.jorgemelo.nimbusfilemanager.security.application.PasswordChangeRequiredInterceptor;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web.LibraryConfigurationInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final LibraryConfigurationInterceptor libraryConfigurationInterceptor;
	private final PasswordChangeRequiredInterceptor passwordChangeRequiredInterceptor;

	public WebMvcConfig(LibraryConfigurationInterceptor libraryConfigurationInterceptor,
			PasswordChangeRequiredInterceptor passwordChangeRequiredInterceptor) {
		this.libraryConfigurationInterceptor = libraryConfigurationInterceptor;
		this.passwordChangeRequiredInterceptor = passwordChangeRequiredInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(libraryConfigurationInterceptor).addPathPatterns("/app/**", "/api/**")
				.excludePathPatterns("/app/onboarding", "/app/account", "/app/account/**", "/app/settings/folders");
		registry.addInterceptor(passwordChangeRequiredInterceptor).addPathPatterns("/app/**", "/api/**")
				.excludePathPatterns("/app/account", "/app/account/password");
	}
}