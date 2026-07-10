package br.com.jorgemelo.nimbusfilemanager.inventory.application.watch.source.rdcw;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Test fake for {@link RdcwReadSeam} that replays a queue of pre-built read
 * results and records whether it was closed.
 */
final class FakeSeam implements RdcwReadSeam {

	private final Deque<RdcwReadResult> results = new ArrayDeque<>();
	private boolean closed;

	void enqueue(RdcwReadResult result) {
		results.add(result);
	}

	boolean closed() {
		return closed;
	}

	@Override
	public RdcwReadResult poll() {
		return results.isEmpty() ? new RdcwReadResult(List.of(), false) : results.poll();
	}

	@Override
	public void close() {
		closed = true;
	}
}