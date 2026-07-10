package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.constants.MetadataConstants;
import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.PhotoPerceptualFingerprint;
import br.com.jorgemelo.nimbusfilemanager.metadata.infrastructure.FfmpegPhotoHashProcessRunner;
import br.com.jorgemelo.nimbusfilemanager.metadata.infrastructure.FfmpegRunner;
import br.com.jorgemelo.nimbusfilemanager.processing.application.ExternalToolGate;
import br.com.jorgemelo.nimbusfilemanager.processing.domain.enums.ExternalToolCategory;
import br.com.jorgemelo.nimbusfilemanager.settings.application.AppSettingService;
import br.com.jorgemelo.nimbusfilemanager.settings.application.constants.SettingsConstants;
import br.com.jorgemelo.nimbusfilemanager.shared.infrastructure.config.properties.dto.NimbusFileManagerProperties;
import br.com.jorgemelo.nimbusfilemanager.shared.util.FileValidationUtils;

/**
 * Computes a 256-bit DCT perceptual hash (pHash) for photos. FFmpeg decodes and
 * normalizes the first frame to a fixed 32x32 grayscale sample. A separable DCT
 * transforms that sample and the 16x16 low-frequency block is thresholded
 * against its AC median to form the compact hash.
 *
 * <p>
 * The normalized luminance sample is returned with the hash and persisted. It
 * is later used for SSIM confirmation, so opening the Similar Photos screen
 * never decodes the original multi-megapixel files again.
 * </p>
 */
@Service
public class PhotoPerceptualHashService {

	static final int LOW_FREQUENCY_SIDE = 16;
	static final int HASH_BITS = LOW_FREQUENCY_SIDE * LOW_FREQUENCY_SIDE;
	private static final double[][] COSINES = cosineTable();

	private final NimbusFileManagerProperties properties;
	private final AppSettingService appSettingService;
	private final FfmpegRunner ffmpegRunner;

	@Autowired
	public PhotoPerceptualHashService(NimbusFileManagerProperties properties, AppSettingService appSettingService,
			ExternalToolGate externalToolGate, FfmpegPhotoHashProcessRunner processRunner) {
		this(properties, appSettingService, (ffmpegPath, file) -> externalToolGate
				.run(ExternalToolCategory.FFMPEG_PHOTO_HASH, () -> processRunner.run(ffmpegPath, file)));
	}

	PhotoPerceptualHashService(NimbusFileManagerProperties properties, AppSettingService appSettingService,
			FfmpegRunner ffmpegRunner) {
		this.properties = properties;
		this.appSettingService = appSettingService;
		this.ffmpegRunner = ffmpegRunner;
	}

	public PhotoPerceptualFingerprint compute(Path file) {
		FileValidationUtils.validateFile(file);

		rejectZipContainerMasqueradingAsWebp(file);

		byte[] pixels;

		try {
			pixels = ffmpegRunner.run(ffmpegPath(), file);
		} catch (Exception exception) {
			throw new IllegalStateException(
					"Could not run ffmpeg to compute perceptual hash for file: " + file + ". " + exception.getMessage(),
					exception);
		}

		if (pixels.length != MetadataConstants.SAMPLE_BYTES) {
			throw new IllegalStateException("Unexpected pixel data size computing perceptual hash for file: " + file
					+ ". Expected " + MetadataConstants.SAMPLE_BYTES + " bytes, got " + pixels.length);
		}

		return new PhotoPerceptualFingerprint(toPHash(pixels), pixels);
	}

	private void rejectZipContainerMasqueradingAsWebp(Path file) {
		String name = file.getFileName().toString().toLowerCase(Locale.ROOT);

		if (!name.endsWith(".webp")) {
			return;
		}

		try (InputStream input = Files.newInputStream(file)) {
			byte[] signature = input.readNBytes(4);

			if (signature.length == 4 && signature[0] == 'P' && signature[1] == 'K'
					&& ((signature[2] == 3 && signature[3] == 4) || (signature[2] == 5 && signature[3] == 6)
							|| (signature[2] == 7 && signature[3] == 8))) {
				throw new UnsupportedPhotoFingerprintException(
						"The .webp file is a ZIP/Lottie package, not a WebP image: " + file);
			}
		} catch (UnsupportedPhotoFingerprintException exception) {
			throw exception;
		} catch (IOException exception) {
			throw new IllegalStateException("Could not inspect WebP signature for file: " + file, exception);
		}
	}

	/** Hamming distance between two 256-bit pHashes. */
	public static int distance(byte[] first, byte[] second) {
		if (first == null || second == null || first.length != MetadataConstants.HASH_BYTES || second.length != MetadataConstants.HASH_BYTES) {
			throw new IllegalArgumentException("Both pHashes must contain exactly 32 bytes");
		}

		int distance = 0;

		for (int index = 0; index < MetadataConstants.HASH_BYTES; index++) {
			distance += Integer.bitCount((first[index] ^ second[index]) & 0xFF);
		}

		return distance;
	}

	/** Maximum 256-bit pHash distance corresponding to a coarse candidate level. */
	static int maxDistanceFor(int similarityPercent) {
		return (int) Math.floor(HASH_BITS * (100 - similarityPercent) / 100.0);
	}

	private byte[] toPHash(byte[] pixels) {
		double[][] rowDct = new double[MetadataConstants.SAMPLE_SIDE][LOW_FREQUENCY_SIDE];

		for (int row = 0; row < MetadataConstants.SAMPLE_SIDE; row++) {
			for (int frequency = 0; frequency < LOW_FREQUENCY_SIDE; frequency++) {
				double sum = 0;
				for (int column = 0; column < MetadataConstants.SAMPLE_SIDE; column++) {
					sum += (pixels[row * MetadataConstants.SAMPLE_SIDE + column] & 0xFF) * COSINES[frequency][column];
				}
				rowDct[row][frequency] = sum;
			}
		}

		double[] coefficients = new double[HASH_BITS];

		int coefficient = 0;

		for (int vertical = 0; vertical < LOW_FREQUENCY_SIDE; vertical++) {
			for (int horizontal = 0; horizontal < LOW_FREQUENCY_SIDE; horizontal++) {
				double sum = 0;

				for (int row = 0; row < MetadataConstants.SAMPLE_SIDE; row++) {
					sum += rowDct[row][horizontal] * COSINES[vertical][row];
				}

				coefficients[coefficient++] = sum;
			}
		}

		double[] ac = Arrays.copyOfRange(coefficients, 1, coefficients.length);

		Arrays.sort(ac);

		double median = ac[ac.length / 2];

		byte[] hash = new byte[MetadataConstants.HASH_BYTES];

		for (int bit = 0; bit < coefficients.length; bit++) {
			if (coefficients[bit] > median) {
				hash[bit / Byte.SIZE] |= (byte) (1 << (7 - bit % Byte.SIZE));
			}
		}

		return hash;
	}

	private static double[][] cosineTable() {
		double[][] table = new double[LOW_FREQUENCY_SIDE][MetadataConstants.SAMPLE_SIDE];

		for (int frequency = 0; frequency < LOW_FREQUENCY_SIDE; frequency++) {
			for (int position = 0; position < MetadataConstants.SAMPLE_SIDE; position++) {
				table[frequency][position] = Math.cos(Math.PI * (2 * position + 1) * frequency / (2.0 * MetadataConstants.SAMPLE_SIDE));
			}
		}

		return table;
	}

	private String ffmpegPath() {
		return appSettingService.stringValue(SettingsConstants.TOOL_FFMPEG, properties.tools().ffmpeg());
	}
}