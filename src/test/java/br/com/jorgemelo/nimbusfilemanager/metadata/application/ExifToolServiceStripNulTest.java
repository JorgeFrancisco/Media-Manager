package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExifToolServiceStripNulTest {

	@Test
	void shouldRemoveNulCharactersSoPostgresTextColumnsAccept() {
		String withNul = "Canon" + (char) 0 + "EOS" + (char) 0;

		Assertions.assertThat(ExifToolService.stripNul(withNul)).isEqualTo("CanonEOS");
		Assertions.assertThat(ExifToolService.stripNul(withNul)).doesNotContain(String.valueOf((char) 0));
	}

	@Test
	void shouldReturnValueUnchangedWhenNoNulAndHandleNull() {
		Assertions.assertThat(ExifToolService.stripNul("clean value")).isEqualTo("clean value");
		Assertions.assertThat(ExifToolService.stripNul(null)).isNull();
	}
}