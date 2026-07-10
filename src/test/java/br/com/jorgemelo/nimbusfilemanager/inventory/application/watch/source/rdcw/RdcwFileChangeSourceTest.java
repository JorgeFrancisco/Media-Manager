package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.rdcw;

import java.nio.file.Path;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RdcwFileChangeSourceTest {

	private static final Path ROOT = Path.of("/library").toAbsolutePath();

	@Test
	void reportsLiveChangesResolvedUnderTheRoot() {
		FakeSeam seam = new FakeSeam();

		seam.enqueue(new RdcwReadResult(List.of("2024\\a.jpg"), false));

		try (RdcwFileChangeSource source = new RdcwFileChangeSource(ROOT, seam, List.of(), false)) {
			Assertions.assertThat(source.pollChangedFiles()).containsExactly(ROOT.resolve("2024").resolve("a.jpg"));
			Assertions.assertThat(source.consumeOverflow()).isFalse();
		}
	}

	@Test
	void deliversTheUsnCatchUpChangesOnlyOnTheFirstPoll() {
		FakeSeam seam = new FakeSeam();

		seam.enqueue(new RdcwReadResult(List.of("live.jpg"), false));
		seam.enqueue(new RdcwReadResult(List.of("live2.jpg"), false));

		Path offline = ROOT.resolve("offline.jpg");

		try (RdcwFileChangeSource source = new RdcwFileChangeSource(ROOT, seam, List.of(offline), false)) {
			Assertions.assertThat(source.pollChangedFiles()).containsExactly(offline, ROOT.resolve("live.jpg"));
			Assertions.assertThat(source.pollChangedFiles()).containsExactly(ROOT.resolve("live2.jpg"));
		}
	}

	@Test
	void propagatesTheSeamOverflowAsAReconcileRequest() {
		FakeSeam seam = new FakeSeam();

		seam.enqueue(new RdcwReadResult(List.of(), true));

		try (RdcwFileChangeSource source = new RdcwFileChangeSource(ROOT, seam, List.of(), false)) {
			source.pollChangedFiles();

			Assertions.assertThat(source.consumeOverflow()).isTrue();
			Assertions.assertThat(source.consumeOverflow()).isFalse();
		}
	}

	@Test
	void requestsAStartupReconcileWhenUsnCatchUpWasUnavailable() {
		try (RdcwFileChangeSource source = new RdcwFileChangeSource(ROOT, new FakeSeam(), List.of(), true)) {
			Assertions.assertThat(source.consumeOverflow()).isTrue();
			Assertions.assertThat(source.consumeOverflow()).isFalse();
		}
	}

	@Test
	void exposesTheRootAndClosesTheSeam() {
		FakeSeam seam = new FakeSeam();

		RdcwFileChangeSource source = new RdcwFileChangeSource(ROOT, seam, List.of(), false);

		Assertions.assertThat(source.root()).isEqualTo(ROOT);

		source.close();

		Assertions.assertThat(seam.closed()).isTrue();
	}
}