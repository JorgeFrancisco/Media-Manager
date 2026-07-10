package br.com.jorgemelo.nimbusfilemanager.security.application;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorService {

	private static final int CODE_DIGITS = 6;
	private static final int TIME_STEP_SECONDS = 30;
	private static final int WINDOW = 1;

	private static final Pattern SIX_DIGIT_CODE = Pattern.compile("\\d{6}");

	private final Clock clock;
	private final SecureRandom secureRandom = new SecureRandom();
	private final Base32 base32 = new Base32();

	@Autowired
	public TwoFactorService() {
		this(Clock.systemUTC());
	}

	TwoFactorService(Clock clock) {
		this.clock = clock;
	}

	public String newSecret() {
		byte[] bytes = new byte[20];

		secureRandom.nextBytes(bytes);

		return base32.encodeToString(bytes).replace("=", "");
	}

	public boolean verify(String secret, String code) {
		if (secret == null || secret.isBlank() || code == null || !SIX_DIGIT_CODE.matcher(code).matches()) {
			return false;
		}

		long counter = clock.instant().getEpochSecond() / TIME_STEP_SECONDS;

		try {
			for (int offset = -WINDOW; offset <= WINDOW; offset++) {
				if (generate(secret, counter + offset).equals(code)) {
					return true;
				}
			}
		} catch (IllegalStateException _) {
			return false;
		}

		return false;
	}

	String generate(String secret, long counter) {
		try {
			byte[] key = base32.decode(secret.toUpperCase(Locale.ROOT));

			byte[] data = ByteBuffer.allocate(Long.BYTES).putLong(counter).array();

			Mac mac = Mac.getInstance("HmacSHA1");

			mac.init(new SecretKeySpec(key, "HmacSHA1"));

			byte[] hash = mac.doFinal(data);

			int offset = hash[hash.length - 1] & 0x0f;

			int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
					| ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

			int otp = binary % 1_000_000;

			return String.format(Locale.ROOT, "%0" + CODE_DIGITS + "d", otp);
		} catch (Exception e) {
			throw new IllegalStateException("Could not generate two-factor code", e);
		}
	}
}