package br.com.jorgemelo.nimbusfilemanager.shared.util;

import java.security.SecureRandom;
import java.util.UUID;

/** Generates RFC 9562 UUID version 7 identifiers. */
public final class UuidV7 {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final long TIMESTAMP_MASK = 0x0000FFFFFFFFFFFFL;

	private UuidV7() {
	}

	public static UUID generate() {
		long timestamp = System.currentTimeMillis() & TIMESTAMP_MASK;
		long mostSignificantBits = timestamp << 16;

		mostSignificantBits |= 0x7000L;
		mostSignificantBits |= RANDOM.nextInt(1 << 12);

		long leastSignificantBits = RANDOM.nextLong();

		leastSignificantBits &= 0x3FFFFFFFFFFFFFFFL;
		leastSignificantBits |= 0x8000000000000000L;

		return new UUID(mostSignificantBits, leastSignificantBits);
	}

	/**
	 * Test/compatibility helper that keeps the UUID shape without exposing a
	 * database key.
	 */
	public static UUID fromLegacy(long value) {
		long mostSignificantBits = 0x0000000000007000L;
		long leastSignificantBits = 0x8000000000000000L | (value & 0x3FFFFFFFFFFFFFFFL);

		return new UUID(mostSignificantBits, leastSignificantBits);
	}

	public static UUID orLegacy(UUID publicId, Long internalId) {
		return publicId != null || internalId == null ? publicId : fromLegacy(internalId);
	}
}