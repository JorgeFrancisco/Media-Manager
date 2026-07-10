package br.com.jorgemelo.nimbusfilemanager.timeline.application.dto;

import java.util.List;

public record TimelineYearCount(int year, long count, List<TimelineMonthCount> months) {

	public TimelineYearCount {
		months = List.copyOf(months);
	}
}