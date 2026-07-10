package br.com.jorgemelo.nimbusfilemanager.shared.util;

import java.util.HashSet;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class UuidV7Test {

	@Test
	void generateShouldCreateUniqueRfc9562VersionSevenIdentifiers() {
		var generated = new HashSet<UUID>();

		for (int index = 0; index < 1_000; index++) {
			UUID uuid = UuidV7.generate();

			Assertions.assertThat(uuid.version()).isEqualTo(7);
			Assertions.assertThat(uuid.variant()).isEqualTo(2);

			generated.add(uuid);
		}

		Assertions.assertThat(generated).hasSize(1_000);
	}
}