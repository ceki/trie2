package ceki.ce;

import java.lang.reflect.Array;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ceki.ce.signal.MixedSignalBarrier;
import ceki.ce.signal.SignalBarier;

public class CylicBuffer<E> {

	static Logger logger = LoggerFactory.getLogger(CylicBuffer.class);

	public final int capacity;
	public final int mask;

	
	final AtomicReferenceArray<E> array;
	final Class<E> clazz;

	static final int MAX_YEILD_COUNT = 2048;

	static final int PARK_DURATION = 1;
	
	
	SignalBarier consumerSignalBarrier = new MixedSignalBarrier(MAX_YEILD_COUNT);
	SignalBarier producerSignalBarrier = new MixedSignalBarrier(MAX_YEILD_COUNT);

	public long sum = 0;
	public long readCount = 0;

	static final int INITIAL_INDEX = -1;
	AtomicInteger writeReserve = new AtomicInteger(INITIAL_INDEX);
	AtomicInteger writeCommit = new AtomicInteger(INITIAL_INDEX);
	AtomicInteger read = new AtomicInteger(INITIAL_INDEX);

	@SuppressWarnings("unchecked")
	CylicBuffer(int capacity, Class<E> clazz) {
		this.capacity = capacity;
		this.mask = capacity - 1;
		this.clazz = clazz;
		this.array = new AtomicReferenceArray<E>(capacity);
	}

	void put(E e) {
		while (true) {
			boolean empty = this.isEmpty();
			boolean success = this.insert(e);
			if (success) {
				if (empty)
					consumerSignalBarrier.signal();
				break;
			} else {
				try {
					producerSignalBarrier.parkNanos(PARK_DURATION);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	int totalConsumed = 0;

	public E[] take() {
		while (true) {
			boolean isFull = isFull();
			Optional<E[]> result = this.consume();
			if (result.isPresent()) {
				if (isFull) {
					producerSignalBarrier.signal();
				}
				E[] values = result.get();
				totalConsumed += values.length;
				return values;
			} else {
				try {
					consumerSignalBarrier.parkNanos(PARK_DURATION);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	boolean insert(E e) {
		int localWriteReserve;
		int localWriteCommit;

		while (true) {

			localWriteReserve = writeReserve.get();
			localWriteCommit = writeCommit.get();

			// logger.debug("writeReserve={} writeCommit={}", writeReserve, writeCommit);

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
		// logger.debug("inserting {} at {} ", e, cyclicWriteSuccessor);
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

	static Integer ONE = new Integer(1);

	public Optional<E[]> consume() {

		final int priorRead = read.get();
		final int localWriteCommit = writeCommit.get();

		if (isEmpty(localWriteCommit, priorRead)) {
			return Optional.empty();
		}


		int count = count(localWriteCommit, priorRead);
		// logger.debug("count="+count);
		@SuppressWarnings("unchecked")
		E[] values = (E[]) Array.newInstance(clazz, count);

		sum += count;
		readCount++;
		for (int i = 0; i < count; i++) {
			values[i] = array.get(getCyclicIndex(priorRead + 1 + i));
			// logger.debug("consumed value {}", values[i]);
		}

		boolean success = read.compareAndSet(priorRead, priorRead + count);
		if (!success) {
			throw new IllegalStateException("only one consumer");
		}
		// logger.debug("consumed {} values", count);

		return Optional.of(values);
	}



	final private boolean isEmpty(int in, int out) {
		return count(in, out) == 0;

	}

	final private boolean isFull(int currentWrite, int currentRead) {
		return count(currentWrite, currentRead) == capacity;
	}

	final private int count(int currentWrite, int currentRead) {
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

	public boolean isFull() {
		return isFull(writeCommit.get(), read.get());
	}
	
	public void barriersDump() {
		System.out.println("consumerSignalBarrier " + consumerSignalBarrier.dump());
		System.out.println("producerSignalBarrier " + producerSignalBarrier.dump());

	}
}
