package ceki.ce;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CylicBuffer<E> {

	static Logger logger = LoggerFactory.getLogger(CylicBuffer.class);

	public final int capacity;
	public final int mask;

	final AtomicReferenceArray<E> array;

	static final int INITIAL_INDEX = -1;
	AtomicInteger writeReserve = new AtomicInteger(INITIAL_INDEX);
	AtomicInteger writeCommit = new AtomicInteger(INITIAL_INDEX);

	AtomicInteger read = new AtomicInteger(INITIAL_INDEX);

	CylicBuffer(int capacity) {
		this.capacity = capacity;
		this.mask = capacity - 1;
		this.array = new AtomicReferenceArray<E>(capacity);
	}

	boolean insert(E e) {
		int localWriteReserve;
		int localWriteCommit;

		while (true) {

			localWriteReserve = writeReserve.get();
			localWriteCommit = writeCommit.get();

			//logger.debug("writeReserve={} writeCommit={}", writeReserve, writeCommit);

			if (localWriteReserve != localWriteCommit)
				continue;

			if (isFull(localWriteReserve, read.get())) {
				return false;
			}

			// reserve the write index at old reserved value + 1
			boolean success = writeReserve.compareAndSet(localWriteReserve, localWriteReserve + 1);
			if (success) {
				break;
			}
		}

		final int writeSuccessor = localWriteReserve + 1;

		final int cyclicWriteSuccessor = getCyclicIndex(writeSuccessor);
		//logger.debug("inserting {} at {} ", e, cyclicWriteSuccessor);
		array.set(cyclicWriteSuccessor, e);

		while (true) {
			boolean success = writeCommit.compareAndSet(localWriteReserve, writeSuccessor);
			if (success) {
				break;
			} else {
				throw new IllegalStateException("unexpeced compare and set");
			}
		}

		// logger.debug("producer writeCommitted.get()={}", writeCommit.get());

		return true;

	}

	public Optional<List<E>> consume() {

		final int priorRead = read.get();
		final int localWriteCommit = writeCommit.get();

		if (isEmpty(localWriteCommit, priorRead)) {
			return Optional.empty();
		}

		int nextOut = getCyclicIndex(priorRead + 1);

		int count = count(localWriteCommit, priorRead);
		//logger.debug("count="+count);
		List<E> values = new ArrayList<>(count);

		if (count < 0) {
			System.out.println("count=" + count);
		}
		for (int i = 0; i < count; i++) {
			values.add(array.get(getCyclicIndex(nextOut + i)));
		}

		boolean success = read.compareAndSet(priorRead, priorRead + count);
		if (!success) {
			throw new IllegalStateException("only one consumer");
		}
		//logger.debug("consumed {} values", count);

		return Optional.of(values);
	}

	private boolean isEmpty(int in, int out) {
		return count(in, out) == 0;

	}

	private boolean isFull(int currentWrite, int currentRead) {
		return count(currentWrite, currentRead) == capacity;
	}

	private int count(int currentWrite, int currentRead) {
		if (currentWrite == INITIAL_INDEX)
			return 0;
		if (currentRead == INITIAL_INDEX) {
			return currentWrite + 1;
		}

		return (currentWrite - currentRead);
	}

	private int getCyclicIndex(int writeIndex) {
		return writeIndex & mask;
	}

	public boolean isEmpty() {
		return isEmpty(writeCommit.get(), read.get());
	}

}
