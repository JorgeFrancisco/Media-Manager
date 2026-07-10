package br.com.jorgemelo.nimbusfilemanager.map.infrastructure.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import br.com.jorgemelo.nimbusfilemanager.map.application.MapService;
import br.com.jorgemelo.nimbusfilemanager.map.application.dto.MapBounds;
import br.com.jorgemelo.nimbusfilemanager.map.application.dto.MapPin;
import br.com.jorgemelo.nimbusfilemanager.map.application.dto.MapPinSource;
import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.FileType;

class MapControllerTest {

	private final MapService mapService = mock(MapService.class);
	private final MapController controller = new MapController(mapService);

	@Test
	void pinsWithoutABoundingBoxLoadsEveryPin() {
		MapPin pin = new MapPin("id", MapPinSource.EXIF, 1.0, 2.0, "label", 3, 2, 1, UUID.randomUUID(), FileType.PHOTO,
				"beach.jpg");

		when(mapService.pins()).thenReturn(List.of(pin));

		assertThat(controller.pins(null, null, null, null, 2000, 3)).containsExactly(pin);
	}

	@Test
	void pinsFallBackToLoadingEveryPinWhenTheBoundingBoxIsIncomplete() {
		when(mapService.pins()).thenReturn(List.of());

		controller.pins(null, -49.0, -25.0, -48.0, 2000, 3);
		controller.pins(-25.0, null, -25.0, -48.0, 2000, 3);
		controller.pins(-25.0, -49.0, null, -48.0, 2000, 3);
		controller.pins(-25.0, -49.0, -25.0, null, 2000, 3);

		verify(mapService, times(4)).pins();
		verify(mapService, never()).pins(any(), anyInt(), anyInt());
	}

	@Test
	void pinsWithABoundingBoxLoadsOnlyTheVisiblePinsAndClampsTheLimit() {
		when(mapService.pins(any(), anyInt(), anyInt())).thenReturn(List.of());

		controller.pins(-25.6, -49.4, -25.4, -49.2, 999999, 12);

		verify(mapService).pins(new MapBounds(-25.6, -49.4, -25.4, -49.2), 5000, 12);
	}

	@Test
	void itemsUsesADefaultPageSizeOf50() {
		when(mapService.items(any(), any())).thenReturn(Page.empty());

		controller.items("pin", 0, 50);

		verify(mapService).items("pin", PageRequest.of(0, 50));
	}

	@Test
	void itemsClampsPageAndSizeToTheAllowedRange() {
		when(mapService.items(any(), any())).thenReturn(Page.empty());

		controller.items("pin", -1, 5000);

		verify(mapService).items("pin", PageRequest.of(0, 200));
	}
}