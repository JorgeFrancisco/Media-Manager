package br.com.jorgemelo.nimbusfilemanager.security.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class QrCodeServiceTest {

	private final QrCodeService service = new QrCodeService();

	@Test
	void pngShouldGeneratePngBytes() {
		byte[] bytes = service.png("otpauth://totp/Media%20Manager:admin?secret=SECRET&issuer=Media%20Manager");

		Assertions.assertThat(bytes).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47)
				.hasSizeGreaterThan(100);
	}

	@Test
	void pngShouldRejectBlankValue() {
		Assertions.assertThatThrownBy(() -> service.png(" ")).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("QR Code value is required.");
	}
}