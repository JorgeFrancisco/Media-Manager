package br.com.jorgemelo.nimbusfilemanager.timeline.application.dto;

import java.util.List;

public record TimelineIndex(long totalItems, long datedItems, long undatedItems, List<TimelineYearCount> years) {

	public TimelineIndex {
		years = List.copyOf(years);
	}
}