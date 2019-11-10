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
	AtomicInteger writeReserved = new AtomicInteger(INITIAL_INDEX);
	AtomicInteger writeCommitted = new AtomicInteger(INITIAL_INDEX);

	AtomicInteger read = new AtomicInteger(INITIAL_INDEX);
	
	CylicBuffer(int capacity) {
		this.capacity = capacity;
		this.mask = capacity - 1;
		this.array = new AtomicReferenceArray<E>(capacity);
	}

	boolean insert(E e) {
		int localWriteReserved;
		int localWriteCommitted;

		while (true) {
			localWriteReserved = writeReserved.get();
			localWriteCommitted = writeCommitted.get();

			if(localWriteReserved != localWriteCommitted)
				continue;
			
			if (isFull(localWriteReserved, read.get())) {
				return false;
			}
			
			boolean success = writeReserved.compareAndSet(localWriteReserved, localWriteReserved+1);
			if (success) {
				break;
			}
		}

		int cyclicWriteIndex = getCyclicIndex(localWriteReserved+1);
		logger.debug("inserting {} at {} ", e, cyclicWriteIndex);
		array.set(cyclicWriteIndex, e);
	
		while (true) {
			boolean success = writeCommitted.compareAndSet(localWriteReserved, localWriteReserved+1);
			if (success) {
				break;
			} else {
				throw new IllegalStateException("unexpeced compare and set");
			}
		}

		logger.debug("producer writeCommitted.get()={}", writeCommitted.get());
		
		return true;
		
	}

	public Optional<E> consume() {

		final int priorOut = read.get();
		
		if (isEmpty(writeCommitted.get(), priorOut)) {
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

	private int xcount(int currentIn, int currentOut) {
		int count = count(currentIn, currentOut);
		logger.trace("count = {} currentIn= {} currentOut= {}", count, currentIn, currentOut);
		return count;
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
			//return currentOut - currentIn + 1;
		}
	}



	
	private int getCyclicIndex(int writeIndex) {
		return writeIndex & mask;
		
//		if (nextOut == capacity) {
//			nextOut = 0;
//		}
//		return nextOut;
	}

//	private void waitForSpace() {
//		if(waitingForData.get())
//			return;
//		
//		waitingForSpace.set(Boolean.TRUE);
//
//		try {
//			lock.lock();
//			spaceAvailableCondition.await();
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		} finally {
//			lock.unlock();
//		}
//	}
//
//	private void notifyOfConsumption() {
//		if(!waitingForSpace.get())
//			return;
//	
//		waitingForSpace.set(Boolean.FALSE);
//		
//		logger.trace("notifyOConsumption() triggered");
//		try {
//			lock.lock();
//			spaceAvailableCondition.signalAll();
//		} finally {
//			lock.unlock();
//		}
//	}
//	
//	private void waitForData() {
//		
//		if(waitingForSpace.get())
//			return;
//		
//		
//		waitingForData.set(Boolean.TRUE);
//
//		try {
//			lock.lock();
//			dataAvailabilityCondition.await();
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		} finally {
//			lock.unlock();
//		}
//	}
//
//	
//	private void notifyOfDataAvailability() {
//		
//		if(!waitingForData.get())
//			return;
//		
//		logger.trace("notifyOfDataAvailability() triggered");
//		waitingForData.set(Boolean.FALSE);
//
//		try {
//			lock.lock();
//			dataAvailabilityCondition.signalAll();
//		} finally {
//			lock.unlock();
//		}
//	}




}
