package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.rdcw;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FileNotifyInformationParserTest {

	@Test
	void parsesASingleRelativePath() {
		Assertions.assertThat(FileNotifyInformationParser.parse(FileNotifyBuffers.buffer("2024\\05\\photo.jpg")))
				.containsExactly("2024\\05\\photo.jpg");
	}

	@Test
	void parsesEveryChainedEntryInOrder() {
		byte[] buffer = FileNotifyBuffers.buffer("a.jpg", "sub\\b.png", "sub\\deep\\c.mp4");

		Assertions.assertThat(FileNotifyInformationParser.parse(buffer)).containsExactly("a.jpg", "sub\\b.png",
				"sub\\deep\\c.mp4");
	}

	@Test
	void decodesUtf16Names() {
		Assertions.assertThat(FileNotifyInformationParser.parse(FileNotifyBuffers.buffer("férias\\praia.jpg")))
				.containsExactly("férias\\praia.jpg");
	}

	@Test
	void returnsEmptyForNullOrShortBuffers() {
		Assertions.assertThat(FileNotifyInformationParser.parse(null)).isEmpty();
		Assertions.assertThat(FileNotifyInformationParser.parse(new byte[4])).isEmpty();
	}
}