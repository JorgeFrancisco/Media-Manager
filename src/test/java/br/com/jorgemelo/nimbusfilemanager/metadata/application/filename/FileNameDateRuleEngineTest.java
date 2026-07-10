package br.com.jorgemelo.nimbusfilemanager.metadata.application.filename;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FileNameDateRuleEngineTest {

	@Test
	void shouldReturnFirstResolvedDateByRuleNameOrder() {
		FileNameDateRule laterRule = rule("020_LATER", LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0));
		FileNameDateRule earlierRule = rule("010_EARLIER", LocalDateTime.of(2010, Month.JANUARY, 1, 0, 0));

		FileNameDateRuleEngine engine = new FileNameDateRuleEngine(List.of(laterRule, earlierRule));

		Assertions.assertThat(engine.resolve("anything.jpg")).isEqualTo(LocalDateTime.of(2010, Month.JANUARY, 1, 0, 0));
	}

	@Test
	void shouldReturnNullForBlankInputOrUnresolvedRules() {
		FileNameDateRuleEngine engine = new FileNameDateRuleEngine(List.of(rule("010_NULL", null)));

		Assertions.assertThat(engine.resolve(null)).isNull();
		Assertions.assertThat(engine.resolve("   ")).isNull();
		Assertions.assertThat(engine.resolve("anything.jpg")).isNull();
	}

	private FileNameDateRule rule(String name, LocalDateTime date) {
		return new FileNameDateRule() {

			@Override
			public boolean supports(String fileName) {
				return true;
			}

			@Override
			public LocalDateTime resolve(String fileName) {
				return date;
			}

			@Override
			public String name() {
				return name;
			}
		};
	}
}