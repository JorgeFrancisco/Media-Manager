package br.com.jorgemelo.nimbusfilemanager.media.infrastructure.rest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgemelo.nimbusfilemanager.media.application.MediaSearchService;
import br.com.jorgemelo.nimbusfilemanager.media.application.dto.MediaSearchCriteria;
import br.com.jorgemelo.nimbusfilemanager.media.application.dto.MediaSearchResponse;
import br.com.jorgemelo.nimbusfilemanager.shared.application.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/media")
public class MediaController {

	private final MediaSearchService mediaSearchService;

	public MediaController(MediaSearchService mediaSearchService) {
		this.mediaSearchService = mediaSearchService;
	}

	@GetMapping
	@Operation(summary = "Search media files")
	public PagedResponse<MediaSearchResponse> search(@ModelAttribute MediaSearchCriteria criteria,
			@PageableDefault(size = 50) Pageable pageable) {
		return PagedResponse.from(mediaSearchService.search(criteria, pageable));
	}
}