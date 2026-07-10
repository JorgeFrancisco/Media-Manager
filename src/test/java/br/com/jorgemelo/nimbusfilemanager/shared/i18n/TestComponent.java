package br.com.jorgemelo.nimbusfilemanager.shared.i18n;

/**
 * Test double that exposes {@link LocalizedComponent#message} so the message
 * resolution behaviour can be exercised directly.
 */
final class TestComponent extends LocalizedComponent {

	String resolve(String key, Object... arguments) {
		return message(key, arguments);
	}
}