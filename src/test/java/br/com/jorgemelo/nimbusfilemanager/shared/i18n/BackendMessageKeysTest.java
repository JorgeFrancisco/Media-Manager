package br.com.jorgemelo.nimbusfilemanager.shared.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Guards the internationalization contract: every backend message key
 * referenced in the source lives in every bundle.
 *
 * <p>
 * {@link LocalizedComponent#message(String, Object...)} no longer carries a
 * hard-coded fallback string — the text is resolved exclusively from the
 * {@code messages*.properties} bundles. That only stays safe if a key can never
 * go missing from a bundle, so this test turns a missing key into a build
 * failure instead of a {@code NoSuchMessageException} in front of a user or,
 * worse, a message shown in the wrong language.
 *
 * <p>
 * Full key parity <em>between</em> the two bundles is enforced separately by
 * {@code MessageBundlesTest}; here the concern is code-to-bundle coverage.
 */
class BackendMessageKeysTest {

	/** Every backend message key is namespaced under {@code backend.} */
	private static final Pattern BACKEND_KEY = Pattern.compile("\"(backend\\.[A-Za-z0-9_.]+)\"");

	private static final Path MAIN_SOURCES = Path.of("src", "main", "java");

	@Test
	void everyBackendKeyUsedInCodeExistsInAllBundles() {
		Set<String> usedKeys = backendKeysReferencedInSource();

		assertThat(usedKeys).as("the scanner should find the backend.* keys used across the code").isNotEmpty();

		Set<String> portuguese = bundleKeys("/messages.properties");

		Set<String> english = bundleKeys("/messages_en.properties");

		assertThat(portuguese).as("keys missing from messages.properties (pt-BR): %s", missing(usedKeys, portuguese))
				.containsAll(usedKeys);

		assertThat(english).as("keys missing from messages_en.properties (en): %s", missing(usedKeys, english))
				.containsAll(usedKeys);
	}

	private Set<String> backendKeysReferencedInSource() {
		try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
			return files.filter(path -> path.toString().endsWith(".java")).flatMap(BackendMessageKeysTest::keysIn)
					.collect(Collectors.toCollection(TreeSet::new));
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not scan " + MAIN_SOURCES, exception);
		}
	}

	private static Stream<String> keysIn(Path javaFile) {
		try {
			Matcher matcher = BACKEND_KEY.matcher(Files.readString(javaFile));

			Stream.Builder<String> keys = Stream.builder();

			while (matcher.find()) {
				keys.add(matcher.group(1));
			}

			return keys.build();
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not read " + javaFile, exception);
		}
	}

	private static Set<String> bundleKeys(String classpathResource) {
		Properties properties = new Properties();

		try (InputStream stream = BackendMessageKeysTest.class.getResourceAsStream(classpathResource)) {
			assertThat(stream).as("bundle %s must be on the classpath", classpathResource).isNotNull();

			properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not load " + classpathResource, exception);
		}

		return properties.stringPropertyNames();
	}

	private static List<String> missing(Set<String> expected, Set<String> actual) {
		return expected.stream().filter(key -> !actual.contains(key)).sorted().toList();
	}
}