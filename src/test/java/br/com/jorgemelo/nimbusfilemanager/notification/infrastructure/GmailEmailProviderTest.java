package br.com.jorgemelo.nimbusfilemanager.notification.infrastructure;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class GmailEmailProviderTest {

	@Test
	void isConfiguredShouldRequireEnabledFlagAndBothCredentials() {
		JavaMailSender mailSender = mock(JavaMailSender.class);

		Assertions.assertThat(new GmailEmailProvider(mailSender, true, "user@gmail.com", "app-password").isConfigured())
				.isTrue();
		Assertions
				.assertThat(new GmailEmailProvider(mailSender, false, "user@gmail.com", "app-password").isConfigured())
				.isFalse();
		Assertions.assertThat(new GmailEmailProvider(mailSender, true, "", "app-password").isConfigured()).isFalse();
		Assertions.assertThat(new GmailEmailProvider(mailSender, true, "user@gmail.com", "").isConfigured()).isFalse();
		Assertions.assertThat(new GmailEmailProvider(mailSender, true, null, null).isConfigured()).isFalse();
	}

	@Test
	void nameShouldIdentifyTheProviderInLogs() {
		GmailEmailProvider provider = new GmailEmailProvider(mock(JavaMailSender.class), true, "user@gmail.com",
				"app-password");

		Assertions.assertThat(provider.name()).isEqualTo("Gmail SMTP");
	}

	@Test
	void sendShouldBuildMessageWithFromToSubjectAndBody() {
		JavaMailSender mailSender = mock(JavaMailSender.class);

		GmailEmailProvider provider = new GmailEmailProvider(mailSender, true, "user@gmail.com", "app-password");

		provider.send("recipient@example.com", "Subject line", "Body text");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		verify(mailSender).send(captor.capture());

		SimpleMailMessage sent = captor.getValue();

		Assertions.assertThat(sent.getFrom()).isEqualTo("user@gmail.com");
		Assertions.assertThat(sent.getTo()).containsExactly("recipient@example.com");
		Assertions.assertThat(sent.getSubject()).isEqualTo("Subject line");
		Assertions.assertThat(sent.getText()).isEqualTo("Body text");
	}
}