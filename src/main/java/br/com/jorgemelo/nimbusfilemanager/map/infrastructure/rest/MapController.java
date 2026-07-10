package br.com.jorgemelo.nimbusfilemanager.map.infrastructure.rest;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgemelo.nimbusfilemanager.map.application.MapService;
import br.com.jorgemelo.nimbusfilemanager.map.application.dto.MapBounds;
import br.com.jorgemelo.nimbusfilemanager.map.application.dto.MapMediaItem;
import br.com.jorgemelo.nimbusfilemanager.map.application.dto.MapPin;
import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Read-only API behind the media map: the aggregated pins and, per pin, its
 * paginated media. Never returns one row per media on the pins call.
 */
@RestController
@RequestMapping("/api/map")
public class MapController {

	private static final int MAX_PAGE_SIZE = 200;
	private static final int MAX_PINS = 5000;

	private final MapService mapService;

	public MapController(MapService mapService) {
		this.mapService = mapService;
	}

	@GetMapping("/pins")
	@Operation(summary = "Aggregated map pins for geo-referenced media (one per location, not per media). "
			+ "Pass the viewport bounding box to load only the pins in view.")
	public List<MapPin> pins(@RequestParam(required = false) Double minLat,
			@RequestParam(required = false) Double minLon, @RequestParam(required = false) Double maxLat,
			@RequestParam(required = false) Double maxLon, @RequestParam(defaultValue = "2000") int limit,
			@RequestParam(defaultValue = "19") int zoom) {
		if (minLat == null || minLon == null || maxLat == null || maxLon == null) {
			return mapService.pins();
		}

		return mapService.pins(new MapBounds(minLat, minLon, maxLat, maxLon), Math.clamp(limit, 1, MAX_PINS), zoom);
	}

	@GetMapping("/items")
	@Operation(summary = "Paginated media of a single pin, by its opaque pinId")
	public PagedResponse<MapMediaItem> items(@RequestParam String pinId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		PageRequest request = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));

		return PagedResponse.from(mapService.items(pinId, request));
	}
}