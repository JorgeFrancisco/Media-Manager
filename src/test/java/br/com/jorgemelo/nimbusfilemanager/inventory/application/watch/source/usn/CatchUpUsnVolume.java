package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.usn;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Test fake for {@link UsnVolume} configured with journal metadata. It records
 * the first offset the catch-up read from and can be told to fail mid-read with
 * a {@link UsnGapException}, so the gap-recovery paths can be exercised.
 */
final class CatchUpUsnVolume implements UsnVolume {

	private final long journalId;
	private final long nextUsn;
	private final long lowestValidUsn;
	private final Deque<UsnReadResult> results = new ArrayDeque<>();
	private Long firstReadFrom;
	private boolean gap;

	CatchUpUsnVolume(long journalId, long nextUsn, long lowestValidUsn) {
		this.journalId = journalId;
		this.nextUsn = nextUsn;
		this.lowestValidUsn = lowestValidUsn;
	}

	void enqueue(UsnReadResult result) {
		results.add(result);
	}

	void failWithGap() {
		this.gap = true;
	}

	Long firstReadFrom() {
		return firstReadFrom;
	}

	@Override
	public long journalId() {
		return journalId;
	}

	@Override
	public long nextUsn() {
		return nextUsn;
	}

	@Override
	public long lowestValidUsn() {
		return lowestValidUsn;
	}

	@Override
	public UsnReadResult readRecords(long fromUsn, int bufferBytes) {
		if (firstReadFrom == null) {
			firstReadFrom = fromUsn;
		}

		if (gap) {
			throw new UsnGapException("aged out");
		}

		return results.isEmpty() ? new UsnReadResult(fromUsn, new byte[0]) : results.poll();
	}

	@Override
	public void close() {
		// no-op
	}
}