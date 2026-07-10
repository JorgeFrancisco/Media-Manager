package br.com.jorgemelo.nimbusfilemanager.notification.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;

class EmailServiceTest {

	@Test
	void sendConfirmationEmailShouldFallBackToLoggingWhenNoProviderIsConfigured() {
		EmailProvider unconfigured = mock(EmailProvider.class);

		when(unconfigured.isConfigured()).thenReturn(false);

		EmailService service = new EmailService(List.of(unconfigured));

		Assertions.assertThatCode(() -> service.sendConfirmationEmail("user@example.com", "http://localhost/confirm"))
				.doesNotThrowAnyException();

		verify(unconfigured, never()).send(anyString(), anyString(), anyString());
	}

	@Test
	void sendConfirmationEmailShouldUseFirstConfiguredProviderInOrder() {
		EmailProvider unconfigured = mock(EmailProvider.class);
		EmailProvider configured = mock(EmailProvider.class);
		EmailProvider alsoConfigured = mock(EmailProvider.class);

		when(unconfigured.isConfigured()).thenReturn(false);
		when(configured.isConfigured()).thenReturn(true);
		when(configured.name()).thenReturn("First Provider");
		when(alsoConfigured.isConfigured()).thenReturn(true);

		EmailService service = new EmailService(List.of(unconfigured, configured, alsoConfigured));

		service.sendConfirmationEmail("user@example.com", "http://localhost/confirm?token=abc");

		verify(configured).send("user@example.com", "Confirme seu cadastro no Nimbus File Manager", """
				Confirme seu cadastro clicando no link abaixo:

				http://localhost/confirm?token=abc

				Esse link expira em 24 horas. Se você não se cadastrou, ignore este e-mail.""");
		verify(unconfigured, never()).send(anyString(), anyString(), anyString());
		verify(alsoConfigured, never()).send(anyString(), anyString(), anyString());
	}

	@Test
	void sendConfirmationEmailShouldNotPropagateWhenProviderThrows() {
		EmailProvider failing = mock(EmailProvider.class);

		when(failing.isConfigured()).thenReturn(true);
		when(failing.name()).thenReturn("Flaky Provider");
		doThrow(new MailException("boom") {

			private static final long serialVersionUID = -1196074615406477951L;
		}).when(failing).send(anyString(), anyString(), anyString());

		EmailService service = new EmailService(List.of(failing));

		Assertions.assertThatCode(() -> service.sendConfirmationEmail("user@example.com", "http://localhost/confirm"))
				.doesNotThrowAnyException();

		verify(failing, times(1)).send(any(), any(), any());
	}
}