package br.com.jorgemelo.nimbusfilemanager.metadata.application.classifier;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;

class MediaSubcategoryRuleEngineTest {

	@Test
	void shouldReturnFirstSupportedRuleByNameOrder() {
		MediaSubcategoryRule later = rule("020_LATER", MediaSubcategory.GOPRO, true);
		MediaSubcategoryRule earlier = rule("010_EARLIER", MediaSubcategory.DRONE, true);

		MediaSubcategoryRuleEngine engine = new MediaSubcategoryRuleEngine(List.of(later, earlier));

		Assertions.assertThat(engine.resolve("anything.jpg", "C:/anything.jpg")).isEqualTo(MediaSubcategory.DRONE);
	}

	@Test
	void shouldFallBackToUnknownWhenNoRuleSupportsOrFileNameIsNull() {
		MediaSubcategoryRuleEngine engine = new MediaSubcategoryRuleEngine(
				List.of(rule("010_NEVER", MediaSubcategory.CAMERA, false)));

		Assertions.assertThat(engine.resolve(null, "C:/anything.jpg")).isEqualTo(MediaSubcategory.UNKNOWN);
		Assertions.assertThat(engine.resolve("anything.jpg", "C:/anything.jpg")).isEqualTo(MediaSubcategory.UNKNOWN);
	}

	private MediaSubcategoryRule rule(String name, MediaSubcategory subcategory, boolean supports) {
		return new MediaSubcategoryRule() {

			@Override
			public boolean supports(String fileName, String path) {
				return supports;
			}

			@Override
			public MediaSubcategory subcategory() {
				return subcategory;
			}

			@Override
			public String name() {
				return name;
			}
		};
	}
}