package br.com.jorgemelo.nimbusfilemanager.shared.application.constants;

/**
 * Contract data constants shared across the application UI. The theme
 * preference key and its values are read both when rendering the shell
 * (AppViewModelAdvice) and when the theme is edited from the Preferencias tab.
 */
public final class SharedConstants {

	public static final String THEME_PREFERENCE_KEY = "theme";
	public static final String THEME_DARK = "dark";
	public static final String THEME_LIGHT = "light";

	private SharedConstants() {
	}
}