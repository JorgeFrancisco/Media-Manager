package br.com.jorgemelo.nimbusfilemanager.shared.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

class LocalizedComponentTest {

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void resolvesMessageUsingCurrentRequestLocaleAndArguments() {
		StaticMessageSource source = new StaticMessageSource();

		source.addMessage("test.count", Locale.ENGLISH, "Moved {0} file(s).");

		TestComponent component = new TestComponent();

		ReflectionTestUtils.setField(component, "messageSource", source);

		LocaleContextHolder.setLocale(Locale.ENGLISH);

		assertThat(component.resolve("test.count", 3)).isEqualTo("Moved 3 file(s).");
	}

	@Test
	void fallsBackToPortugueseBaseBundleWhenNoMessageSourceIsInjected() {
		TestComponent component = new TestComponent();

		assertThat(component.resolve("backend.account.userNotFound")).isEqualTo("Usuário não encontrado.");
	}

	@Test
	void substitutesArgumentsWhenResolvingFromTheBaseBundle() {
		TestComponent component = new TestComponent();

		assertThat(component.resolve("backend.quarantine.batchCompleted", 2, 1, 0, 3))
				.isEqualTo("Restauração concluída. restaurados=2, conflitos=1, origem ausente=0, erros=3.");
	}

	@Test
	void raisesWhenAKeyIsAbsentFromTheBaseBundle() {
		TestComponent component = new TestComponent();

		assertThatThrownBy(() -> component.resolve("backend.does.not.exist"))
				.isInstanceOf(NoSuchMessageException.class);
	}
}