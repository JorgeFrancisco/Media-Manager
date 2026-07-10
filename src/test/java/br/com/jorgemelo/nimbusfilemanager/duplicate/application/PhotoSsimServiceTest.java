package br.com.jorgemelo.nimbusfilemanager.duplicate.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class PhotoSsimServiceTest {

	private final PhotoSsimService service = new PhotoSsimService();

	@Test
	void identicalSamplesAreOneHundredPercent() {
		byte[] sample = filled(120);

		assertThat(service.similarityPercent(sample, sample.clone())).isEqualTo(100);
	}

	@Test
	void veryDifferentSamplesAreNotReportedAsIdentical() {
		assertThat(service.similarityPercent(filled(0), filled(255))).isLessThan(10);
	}

	@Test
	void smallLuminanceChangeRemainsHighlySimilar() {
		assertThat(service.similarityPercent(filled(120), filled(125))).isGreaterThanOrEqualTo(95);
	}

	private byte[] filled(int value) {
		byte[] sample = new byte[1024];

		Arrays.fill(sample, (byte) value);

		return sample;
	}
}