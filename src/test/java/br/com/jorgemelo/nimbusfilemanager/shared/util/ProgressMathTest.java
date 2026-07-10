package br.com.jorgemelo.nimbusfilemanager.shared.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ProgressMathTest {

	@Test
	void percentShouldBeProportionalAndCapped() {
		Assertions.assertThat(ProgressMath.percent(0, 100)).isZero();
		Assertions.assertThat(ProgressMath.percent(25, 100)).isEqualTo(25);
		Assertions.assertThat(ProgressMath.percent(100, 100)).isEqualTo(100);
		Assertions.assertThat(ProgressMath.percent(150, 100)).isEqualTo(100);
	}

	@Test
	void percentShouldBeUnknownWithoutTotal() {
		Assertions.assertThat(ProgressMath.percent(50, 0)).isEqualTo(-1);
		Assertions.assertThat(ProgressMath.percent(50, -1)).isEqualTo(-1);
	}

	@Test
	void etaShouldProjectRemainingTimeFromAverageRate() {
		// 10s elapsed for 25 of 100: 75 remaining at 2.5/s -> 30s.
		Assertions.assertThat(ProgressMath.etaSeconds(10_000, 25, 100)).isEqualTo(30);
	}

	@Test
	void etaShouldBeUnknownWhenRateIsMeaninglessOrNoisy() {
		Assertions.assertThat(ProgressMath.etaSeconds(10_000, 0, 100)).isEqualTo(-1);
		Assertions.assertThat(ProgressMath.etaSeconds(10_000, 50, 0)).isEqualTo(-1);
		Assertions.assertThat(ProgressMath.etaSeconds(10_000, 150, 100)).isEqualTo(-1);
		Assertions.assertThat(ProgressMath.etaSeconds(500, 25, 100)).isEqualTo(-1);
	}
}