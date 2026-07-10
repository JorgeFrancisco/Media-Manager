package br.com.jorgemelo.nimbusfilemanager.security.infrastructure.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import br.com.jorgemelo.nimbusfilemanager.security.application.UserAccessLogService;

class AccessLogWebControllerTest {

	@Test
	void accessesShouldSearchHistoryByEmailOnlyWhenRequested() {
		UserAccessLogService userAccessLogService = mock(UserAccessLogService.class);
		AccessLogWebController controller = new AccessLogWebController(userAccessLogService);
		ExtendedModelMap initialModel = new ExtendedModelMap();
		ExtendedModelMap searchModel = new ExtendedModelMap();

		Assertions.assertThat(controller.accesses(null, initialModel)).isEqualTo("app/accesses");
		Assertions.assertThat(initialModel).containsEntry("searched", false).containsEntry("accessLogs", List.of());

		Assertions.assertThat(controller.accesses(" admin@nimbus-file-manager.local ", searchModel))
				.isEqualTo("app/accesses");
		Assertions.assertThat(searchModel).containsEntry("email", "admin@nimbus-file-manager.local")
				.containsEntry("searched", true);
		verify(userAccessLogService).findByEmail("admin@nimbus-file-manager.local");
	}
}