package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class PhashBacklogStatusTest {

	@Test
	void totalBlockingAndPercentWhilePending() {
		PhashBacklogStatus status = new PhashBacklogStatus(5, 3, 2);

		Assertions.assertThat(status.total()).isEqualTo(10);
		Assertions.assertThat(status.blocking()).isTrue();
		Assertions.assertThat(status.percent()).isEqualTo(50);
	}

	@Test
	void unblocksAtHundredPercentEvenWithFailures() {
		PhashBacklogStatus status = new PhashBacklogStatus(0, 8, 2);

		Assertions.assertThat(status.blocking()).isFalse();
		Assertions.assertThat(status.percent()).isEqualTo(100);
	}

	@Test
	void emptyLibraryIsComplete() {
		PhashBacklogStatus status = new PhashBacklogStatus(0, 0, 0);

		Assertions.assertThat(status.blocking()).isFalse();
		Assertions.assertThat(status.percent()).isEqualTo(100);
	}
}