package br.com.jorgemelo.nimbusfilemanager.timeline.infrastructure.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.enums.MediaSubcategory;
import br.com.jorgemelo.nimbusfilemanager.timeline.application.TimelineService;
import br.com.jorgemelo.nimbusfilemanager.timeline.application.dto.TimelineIndex;
import br.com.jorgemelo.nimbusfilemanager.timeline.application.dto.TimelinePageResponse;
import br.com.jorgemelo.nimbusfilemanager.timeline.application.dto.TimelineUndatedPageResponse;
import br.com.jorgemelo.nimbusfilemanager.timeline.domain.enums.TimelineMediaType;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

	private final TimelineService timelineService;

	public TimelineController(TimelineService timelineService) {
		this.timelineService = timelineService;
	}

	@GetMapping("/index")
	@Operation(summary = "Returns Timeline counts grouped by year and month")
	public TimelineIndex index(@RequestParam(defaultValue = "ALL") TimelineMediaType type,
			@RequestParam(required = false) List<MediaSubcategory> subcategories) {
		return timelineService.index(type.fileType(), subcategories);
	}

	@GetMapping("/items")
	@Operation(summary = "Returns a keyset-paginated Timeline page grouped by capture day")
	public TimelinePageResponse items(@RequestParam(defaultValue = "ALL") TimelineMediaType type,
			@RequestParam(required = false) List<MediaSubcategory> subcategories,
			@RequestParam(defaultValue = "120") int limit, @RequestParam(required = false) String cursor,
			@RequestParam(required = false) LocalDate from) {
		return timelineService.page(type, subcategories, limit, cursor, from);
	}

	@GetMapping("/undated")
	@Operation(summary = "Returns visual media without a resolved capture date")
	public TimelineUndatedPageResponse undated(@RequestParam(defaultValue = "ALL") TimelineMediaType type,
			@RequestParam(required = false) List<MediaSubcategory> subcategories,
			@RequestParam(defaultValue = "120") int limit, @RequestParam(required = false) String cursor) {
		return timelineService.undated(type, subcategories, limit, cursor);
	}
}