package ceki.ce;

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

			if(localWriteReserve != localWriteCommit)
				continue;
			
			if (isFull(localWriteReserve, read.get())) {
				return false;
			}
			
			boolean success = writeReserve.compareAndSet(localWriteReserve, localWriteReserve+1);
			if (success) {
				break;
			}
		}

		final int writeSuccessor = localWriteReserve+1;
		
		final int cyclicWriteSuccessor = getCyclicIndex(writeSuccessor);
		logger.debug("inserting {} at {} ", e, cyclicWriteSuccessor);
		array.set(cyclicWriteSuccessor, e);
	
		while (true) {
			boolean success = writeCommit.compareAndSet(localWriteReserve, writeSuccessor);
			if (success) {
				break;
			} else {
				throw new IllegalStateException("unexpeced compare and set");
			}
		}

		logger.debug("producer writeCommitted.get()={}", writeCommit.get());
		
		return true;
		
	}

	public Optional<E> consume() {

		final int priorOut = read.get();
		
		if (isEmpty(writeCommit.get(), priorOut)) {
			return Optional.empty();
		}

		int nextOut = getCyclicIndex(priorOut);
		E e = array.get(nextOut);

		boolean success = read.compareAndSet(priorOut, priorOut+1);
		if (!success) {
			throw new IllegalStateException("only one consumer");
		}
		logger.debug("consuming {} at nextOut={}", e, nextOut);

		return Optional.of(e);
	}

	
	private boolean isEmpty(int in, int out) {
		return count(in, out) == 0;

	}

	private boolean isFull(int currentIn, int currentOut) {
		return count(currentIn, currentOut) == capacity;
	}


	private int count(int currentIn, int currentOut) {
		if (currentIn == INITIAL_INDEX)
			return 0;
		if (currentOut == INITIAL_INDEX) {
			return currentIn + 1;
		}

		if (currentIn >= currentOut)
			return (currentIn - currentOut);
		else {
			throw new IllegalStateException("currentIn = " + currentIn + " <  currentOut = "+currentOut);
		}
	}


	
	private int getCyclicIndex(int writeIndex) {
		return writeIndex & mask;
	}		




}
