package br.com.jorgemelo.nimbusfilemanager.security.application;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrCodeService {

	private static final int SIZE = 220;

	public byte[] png(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("QR Code value is required.");
		}

		try {
			BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, SIZE, SIZE);

			ByteArrayOutputStream output = new ByteArrayOutputStream();

			MatrixToImageWriter.writeToStream(matrix, "PNG", output);

			return output.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("Could not generate QR Code.", e);
		}
	}
}