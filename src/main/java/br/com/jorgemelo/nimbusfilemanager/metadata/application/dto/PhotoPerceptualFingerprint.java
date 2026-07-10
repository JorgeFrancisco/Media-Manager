package br.com.jorgemelo.nimbusfilemanager.metadata.application.dto;

import java.util.Arrays;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.constants.MetadataConstants;

/**
 * A 256-bit DCT pHash plus the normalized 32x32 luminance sample from which it
 * was derived. The sample lets candidate pairs be verified with SSIM without
 * reopening the original multi-megapixel photos.
 */
public record PhotoPerceptualFingerprint(byte[] hash, byte[] luminance) {

	public PhotoPerceptualFingerprint {
		if (hash == null || hash.length != MetadataConstants.HASH_BYTES) {
			throw new IllegalArgumentException("pHash must contain exactly 32 bytes");
		}

		if (luminance == null || luminance.length != MetadataConstants.SAMPLE_BYTES) {
			throw new IllegalArgumentException("Luminance sample must contain exactly 1024 bytes");
		}

		hash = hash.clone();
		luminance = luminance.clone();
	}

	@Override
	public byte[] hash() {
		return hash.clone();
	}

	@Override
	public byte[] luminance() {
		return luminance.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PhotoPerceptualFingerprint other = (PhotoPerceptualFingerprint) o;

		return Arrays.equals(hash, other.hash) && Arrays.equals(luminance, other.luminance);
	}

	@Override
	public int hashCode() {
		return 31 * Arrays.hashCode(hash) + Arrays.hashCode(luminance);
	}

	@Override
	public String toString() {
		return "PhotoPerceptualFingerprint[hash=" + Arrays.toString(hash) + ", luminance=" + Arrays.toString(luminance)
				+ "]";
	}
}