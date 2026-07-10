package br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Guards the physical-only policy at the UI level: the welcome screen must not
 * offer any "follow links" option.
 */
class OnboardingTemplateTest {

	@Test
	void onboardingScreenHasNoFollowLinksOption() throws Exception {
		String html = Files.readString(Path.of("src/main/resources/templates/app/onboarding.html"));

		assertThat(html).doesNotContain("followLinks").doesNotContain("> Links<");
	}
}