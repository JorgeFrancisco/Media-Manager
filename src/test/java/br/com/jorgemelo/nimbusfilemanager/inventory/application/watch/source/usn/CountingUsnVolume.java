package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.usn;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Test fake for {@link UsnVolume} that replays a fixed list of read results and
 * counts how many reads the reader issued, so tests can assert it stops instead
 * of spinning.
 */
final class CountingUsnVolume implements UsnVolume {

	private final Deque<UsnReadResult> results;
	private int reads;

	CountingUsnVolume(List<UsnReadResult> results) {
		this.results = new ArrayDeque<>(results);
	}

	int reads() {
		return reads;
	}

	@Override
	public long journalId() {
		return 1L;
	}

	@Override
	public long nextUsn() {
		return 0L;
	}

	@Override
	public long lowestValidUsn() {
		return 0L;
	}

	@Override
	public UsnReadResult readRecords(long fromUsn, int bufferBytes) {
		reads++;

		return results.isEmpty() ? new UsnReadResult(fromUsn, new byte[0]) : results.poll();
	}

	@Override
	public void close() {
		// no-op
	}
}