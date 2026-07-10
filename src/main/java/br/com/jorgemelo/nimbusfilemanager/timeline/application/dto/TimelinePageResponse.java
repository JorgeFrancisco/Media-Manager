package br.com.jorgemelo.nimbusfilemanager.timeline.application.dto;

import java.util.List;

public record TimelinePageResponse(List<TimelineDayResponse> groups, String nextCursor, boolean hasMore) {

	public TimelinePageResponse {
		groups = List.copyOf(groups);
	}
}