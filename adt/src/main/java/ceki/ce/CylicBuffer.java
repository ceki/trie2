package ceki.ce;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CylicBuffer<E> {

	static Logger logger = LoggerFactory.getLogger(CylicBuffer.class);

	public final int size;

	final Object[] array;

	static final int INITIAL_INDEX = -1;
	AtomicInteger inAtomic = new AtomicInteger(INITIAL_INDEX);

	AtomicInteger outAtomic = new AtomicInteger(INITIAL_INDEX);

	ReentrantLock lock = new ReentrantLock();
	Condition spaceAvailableCondition = lock.newCondition();
	Condition dataAvailabilityCondition = lock.newCondition();

	CylicBuffer(int size) {
		this.size = size;
		this.array = new Object[size];
	}

	void insert(E e) {

		int priorIn;
		int nextIn;
		int priorOut;

		while (true) {
			priorIn = inAtomic.getAcquire();
			priorOut = outAtomic.getAcquire();
			nextIn = cyclicIncrement(priorIn);

			if (isFull(priorIn, priorOut)) {
				logger.debug("waiting for space");
				waitForSpace();
				logger.debug("notified of space");
			}

			boolean success = inAtomic.compareAndSet(priorIn, nextIn);
			if (success) {
				break;
			}
		}

		logger.debug("inserting {} at {} ", e, nextIn);
		array[nextIn] = e;

		if (isEmpty(priorIn, priorOut)) {
			notifyOfDataAvailability();
		}

	}

	private boolean isEmpty(int in, int out) {
		return count(in, out) == 0;

	}

	private boolean isFull(int currentIn, int currentOut) {
		return count(currentIn, currentOut) == size;
	}

	private int count(int currentIn, int currentOut) {
		int count = _count(currentIn, currentOut);
		logger.debug("count = {} currentIn= {} currentOut= {}", count, currentIn, currentOut);
		return count;
	}

	private int _count(int currentIn, int currentOut) {
		if(currentIn == INITIAL_INDEX)
		  return 0;
		if(currentOut == INITIAL_INDEX) {
			return currentIn;
		}

		if (currentIn >= currentOut)
			return currentIn - currentOut + 1;
		else
			return currentOut - currentIn + 1;
	}

	@SuppressWarnings("unchecked")
	public E consume() {
		int priorIn;
		int priorOut;
		int nextOut;

		while (true) {
			priorIn = inAtomic.getAcquire();
			priorOut = outAtomic.getAcquire();
			nextOut = cyclicIncrement(priorOut);
			if (isEmpty(priorIn, priorOut)) {
				logger.debug("waiting for data");
				waitForData();
				logger.debug("notified for data");
			}
			boolean success = outAtomic.compareAndSet(priorOut, nextOut);
			if (success) {
				break;
			}
		}

		Object o = array[nextOut];

		if (isFull(priorIn, priorOut)) {
			notifyOConsumption();
		}

		logger.debug("consuming {} at {} ", o, nextOut);
		return (E) o;
	}

	private int cyclicIncrement(int priorOut) {
		int nextOut = priorOut + 1;
		if (nextOut == size) {
			nextOut = 0;
		}
		return nextOut;
	}

	private void waitForSpace() {
		try {
			lock.lock();
			spaceAvailableCondition.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	private void notifyOfDataAvailability() {
		logger.debug("notifyOfDataAvailability() called");

		try {
			lock.lock();
			dataAvailabilityCondition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	private void waitForData() {
		try {
			lock.lock();
			dataAvailabilityCondition.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	private void notifyOConsumption() {
		logger.debug("notifyOConsumption() called");
		try {
			lock.lock();
			spaceAvailableCondition.signalAll();
		} finally {
			lock.unlock();
		}
	}

}
