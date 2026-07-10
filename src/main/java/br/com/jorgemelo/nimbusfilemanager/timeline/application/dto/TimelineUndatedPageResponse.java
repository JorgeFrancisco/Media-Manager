package br.com.jorgemelo.nimbusfilemanager.timeline.application.dto;

import java.util.List;

public record TimelineUndatedPageResponse(List<TimelineItemResponse> items, String nextCursor, boolean hasMore) {

	public TimelineUndatedPageResponse {
		items = List.copyOf(items);
	}
}