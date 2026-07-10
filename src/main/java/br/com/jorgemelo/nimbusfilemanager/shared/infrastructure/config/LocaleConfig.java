package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import br.com.jorgemelo.nimbusfilemanager.preferences.application.UserPagePreferenceService;

/**
 * Wires the internationalization plumbing: a {@link LocaleResolver} backed by
 * the user's saved preference and a {@link LocaleChangeInterceptor} that lets
 * any request switch language via a {@code ?lang=} parameter (used by the
 * language selector in Preferences). Switching takes effect on the same
 * response and is persisted per user - no application restart is needed.
 */
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

	/**
	 * Must be named "localeResolver" so Spring MVC picks it over the default
	 * header-based one.
	 */
	@Bean
	public LocaleResolver localeResolver(UserPagePreferenceService userPagePreferenceService) {
		return new UserPreferenceLocaleResolver(userPagePreferenceService);
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();

		interceptor.setParamName("lang");
		interceptor.setIgnoreInvalidLocale(true);

		return interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}
}