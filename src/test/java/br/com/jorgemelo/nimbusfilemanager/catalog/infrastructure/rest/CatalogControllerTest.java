package br.com.jorgemelo.nimbusfilemanager.catalog.infrastructure.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.com.jorgemelo.nimbusfilemanager.catalog.application.CatalogExportService;
import br.com.jorgemelo.nimbusfilemanager.catalog.application.dto.CatalogExport;
import br.com.jorgemelo.nimbusfilemanager.catalog.domain.enums.CatalogExportFormat;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

	@Mock
	private CatalogExportService catalogExportService;

	@Test
	void exportShouldStreamWithAttachmentHeaders() {
		when(catalogExportService.export(any()))
				.thenReturn(new CatalogExport("catalog-20260713.csv", "text/csv; charset=UTF-8", _ -> {
				}));

		var response = controller().export("csv");

		Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
		Assertions.assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
				.isEqualTo("attachment; filename=catalog-20260713.csv");
		Assertions.assertThat(response.getHeaders().getContentType().toString()).contains("text/csv");
		Assertions.assertThat(response.getBody()).isNotNull();
	}

	@Test
	void exportShouldDefaultToCsvWhenFormatIsBlank() {
		when(catalogExportService.export(CatalogExportFormat.CSV))
				.thenReturn(new CatalogExport("catalog.csv", "text/csv; charset=UTF-8", _ -> {
				}));

		var response = controller().export("");

		Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
	}

	@Test
	void exportShouldRejectUnknownFormatWithBadRequest() {
		CatalogController controller = controller();

		Assertions.assertThatThrownBy(() -> controller.export("xml")).isInstanceOf(ResponseStatusException.class)
				.satisfies(e -> Assertions.assertThat(((ResponseStatusException) e).getStatusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST));
	}

	private CatalogController controller() {
		return new CatalogController(catalogExportService);
	}
}