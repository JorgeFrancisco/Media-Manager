package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.usn;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UsnReadResultTest {

	@Test
	void nullRecordsBecomeAnEmptyDrainedResult() {
		UsnReadResult result = new UsnReadResult(42L, null);

		Assertions.assertThat(result.records()).isEmpty();
		Assertions.assertThat(result.drained()).isTrue();
		Assertions.assertThat(result.nextStartUsn()).isEqualTo(42L);
	}

	@Test
	void nonEmptyRecordsAreNotDrained() {
		Assertions.assertThat(new UsnReadResult(1L, new byte[] { 1, 2, 3 }).drained()).isFalse();
	}

	@Test
	void equalsHashCodeAndToStringConsiderTheRecordBytes() {
		UsnReadResult one = new UsnReadResult(5L, new byte[] { 1, 2 });
		UsnReadResult same = new UsnReadResult(5L, new byte[] { 1, 2 });
		UsnReadResult differentBytes = new UsnReadResult(5L, new byte[] { 9 });
		UsnReadResult differentUsn = new UsnReadResult(6L, new byte[] { 1, 2 });

		Assertions.assertThat(one).isEqualTo(same).hasSameHashCodeAs(same).isNotEqualTo(differentBytes)
				.isNotEqualTo(differentUsn).isNotEqualTo(null).isNotEqualTo("x")
				.hasToString("UsnReadResult[nextStartUsn=5, records=2 bytes]");
	}
}