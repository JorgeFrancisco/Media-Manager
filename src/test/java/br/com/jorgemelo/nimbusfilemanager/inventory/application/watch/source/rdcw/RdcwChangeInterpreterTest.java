package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.rdcw;

import java.nio.file.Path;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RdcwChangeInterpreterTest {

	private static final Path ROOT = Path.of("/library").toAbsolutePath();

	private final RdcwChangeInterpreter interpreter = new RdcwChangeInterpreter(ROOT);

	@Test
	void resolvesBackSlashRelativePathsUnderTheRoot() {
		List<Path> changed = interpreter.interpret(List.of("2024\\05\\photo.jpg", "top.jpg"));

		Assertions.assertThat(changed).containsExactly(ROOT.resolve("2024").resolve("05").resolve("photo.jpg"),
				ROOT.resolve("top.jpg"));
	}

	@Test
	void deduplicatesRepeatedPathsWithinABatch() {
		Assertions.assertThat(interpreter.interpret(List.of("a\\b.jpg", "a\\b.jpg")))
				.containsExactly(ROOT.resolve("a").resolve("b.jpg"));
	}

	@Test
	void dropsPathsThatEscapeTheRoot() {
		Assertions.assertThat(interpreter.interpret(List.of("..\\..\\outside.jpg"))).isEmpty();
	}
}