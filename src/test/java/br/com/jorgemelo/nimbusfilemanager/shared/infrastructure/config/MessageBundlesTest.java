package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config;

import java.io.InputStream;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Keeps the pt-BR (default) and English message bundles in sync so no UI string
 * is left untranslated.
 */
class MessageBundlesTest {

	@Test
	void bothBundlesExposeExactlyTheSameKeys() throws Exception {
		Properties ptBr = load("/messages.properties");
		Properties english = load("/messages_en.properties");

		Assertions.assertThat(ptBr.stringPropertyNames()).isNotEmpty();
		Assertions.assertThat(english.keySet())
				.as("English bundle must define exactly the same keys as the default pt-BR bundle")
				.isEqualTo(ptBr.keySet());
	}

	private Properties load(String resource) throws Exception {
		Properties properties = new Properties();

		try (InputStream input = MessageBundlesTest.class.getResourceAsStream(resource)) {
			Assertions.assertThat(input).as("bundle %s must exist on the classpath", resource).isNotNull();

			properties.load(input);
		}

		return properties;
	}
}