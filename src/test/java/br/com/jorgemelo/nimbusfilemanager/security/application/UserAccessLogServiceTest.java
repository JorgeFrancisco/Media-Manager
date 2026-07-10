package br.com.jorgemelo.nimbusfilemanager.security.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.jorgemelo.nimbusfilemanager.security.application.constants.SecurityConstants;
import br.com.jorgemelo.nimbusfilemanager.security.domain.model.UserAccessLog;
import br.com.jorgemelo.nimbusfilemanager.security.domain.repository.UserAccessLogRepository;

class UserAccessLogServiceTest {

	@Test
	void recordShouldStoreAccessContext() {
		UserAccessLogRepository repository = mock(UserAccessLogRepository.class);

		UserAccessLogService service = new UserAccessLogService(repository);

		when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		service.recordAccess("admin", SecurityConstants.LOGIN_SUCCESS, "SUCCESS", "10.0.0.1", "JUnit",
				"Login completed.");

		ArgumentCaptor<UserAccessLog> captor = ArgumentCaptor.forClass(UserAccessLog.class);

		verify(repository).save(captor.capture());

		Assertions.assertThat(captor.getValue().getUsername()).isEqualTo("admin");
		Assertions.assertThat(captor.getValue().getEventType()).isEqualTo(SecurityConstants.LOGIN_SUCCESS);
		Assertions.assertThat(captor.getValue().getStatus()).isEqualTo("SUCCESS");
		Assertions.assertThat(captor.getValue().getIpAddress()).isEqualTo("10.0.0.1");
		Assertions.assertThat(captor.getValue().getUserAgent()).isEqualTo("JUnit");
		Assertions.assertThat(captor.getValue().getMessage()).isEqualTo("Login completed.");
	}

	@Test
	void findByEmailShouldTrimAndIgnoreBlankSearch() {
		UserAccessLogRepository repository = mock(UserAccessLogRepository.class);

		UserAccessLogService service = new UserAccessLogService(repository);

		Assertions.assertThat(service.findByEmail(" ")).isEmpty();

		verify(repository, never()).findByUsernameIgnoreCaseOrderByCreatedAtDesc(any());

		service.findByEmail(" admin@nimbus-file-manager.local ");

		verify(repository).findByUsernameIgnoreCaseOrderByCreatedAtDesc("admin@nimbus-file-manager.local");
	}
}