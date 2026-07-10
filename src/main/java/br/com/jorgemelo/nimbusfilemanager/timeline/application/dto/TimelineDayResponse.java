package br.com.jorgemelo.nimbusfilemanager.timeline.application.dto;

import java.time.LocalDate;
import java.util.List;

public record TimelineDayResponse(LocalDate date, List<TimelineItemResponse> items) {

	public TimelineDayResponse {
		items = List.copyOf(items);
	}
}