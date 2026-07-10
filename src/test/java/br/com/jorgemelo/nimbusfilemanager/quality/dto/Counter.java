package br.com.jorgemelo.nimbusfilemanager.quality.dto;

public record Counter(long missed, long covered) {

	public double percentage() {
		long total = missed + covered;

		if (total == 0) {
			return 0.0;
		}

		return covered * 100.0 / total;
	}
}