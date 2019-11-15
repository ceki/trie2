package ch.qos.ringBuffer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.ringBuffer.signal.SignalBarrier;
import ch.qos.ringBuffer.signal.SignalBarrierFactory;

public class DoubleWriterLockedRingBuffer<E> implements RingBuffer<E> {

	static Logger logger = LoggerFactory.getLogger(DoubleWriterLockedRingBuffer.class);

	public final int capacity;
	public final int mask;

	final AtomicReferenceArray<E> array;
	final Class<E> clazz;

	SignalBarrier consumerSignalBarrier = SignalBarrierFactory.makeSignalBarrier();
	SignalBarrier producerSignalBarrier =  SignalBarrierFactory.makeSignalBarrier();

	static final long INITIAL_INDEX = -1;
	AtomicLong writeReserve = new AtomicLong(INITIAL_INDEX);
	AtomicLong writeCommit = new AtomicLong(INITIAL_INDEX);

	AtomicLong read = new AtomicLong(INITIAL_INDEX);

	DoubleWriterLockedRingBuffer(int capacity, Class<E> clazz) {
		this.capacity = capacity;
		this.mask = capacity - 1;
		this.clazz = clazz;
		this.array = new AtomicReferenceArray<E>(capacity);
	}

	
	int lastPutCount = 0;
	@Override
	public void put(E e) {
		int count = 0;

		while (true) {

			long localReadCache = read.getAcquire();
			long locaWriteCache = writeCommit.getAcquire();
			boolean empty = this.isEmpty(locaWriteCache, localReadCache);
			boolean success = this.insert(e, locaWriteCache, localReadCache);
			if (success) {
				lastPutCount = count;
				if (empty)
					consumerSignalBarrier.signal();
				break;
			} else {
				count++;
				try {
					producerSignalBarrier.await(lastPutCount);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	
	 int lastTakeCount = 1;
	
	@Override
	public E take() {
		int count = 0;
		while (true) {
			long cachedWriteCommit = writeCommit.getAcquire();
			long cachedRead = read.getAcquire();
			boolean wasFull = isFull(cachedWriteCommit, cachedRead);
			E result = this.consume(cachedWriteCommit, cachedRead);
			if (result != null) {
				if (wasFull) {
					producerSignalBarrier.signal();
				}
				// totalConsumed += result.length;
				lastTakeCount = count;
				return result;
			} else {
				try {
					consumerSignalBarrier.await(lastTakeCount);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private boolean insert(E e, long writeCommitCache, long readCache) {
		long localWriteReserve;
		long localWriteCommit = writeCommitCache;
		long localReadCache = readCache;

		while (true) {

			localWriteReserve = writeReserve.getAcquire();
			
			// logger.debug("insert/while - writeReserve={} writeCommit={}", writeReserve,
			// writeCommit);

			if (localWriteReserve != localWriteCommit) {
				localWriteCommit = writeCommit.getAcquire();
				if(localWriteReserve != localWriteCommit)
				continue;
			}

			if (isFull(localWriteReserve, localReadCache)) {
				localReadCache = read.getAcquire();
				if (isFull(localWriteReserve, localReadCache)) {
					// logger.debug("insert/ full - localWriteReserve={} localReadCache={}",
					// localWriteReserve, localReadCache);
					return false;
				}
			}

			// reserve the write index at old reserved value + 1
			boolean success = writeReserve.compareAndSet(localWriteReserve, localWriteReserve + 1);
			if (success) {
				break;
			}
		}

		final long writeSuccessor = localWriteCommit + 1;
		final int cyclicWriteSuccessor = getCyclicIndex(writeSuccessor);

		// logger.debug("inserted e={} at next={}", e, writeSuccessor);

		array.set(cyclicWriteSuccessor, e);
		writeCommit.set(writeSuccessor);

		return true;

	}

	private E consume(long cachedWriteCommit, long cachedRead) {

		long localRead = cachedRead;
				
		long localWriteCommit = cachedWriteCommit;

		// logger.debug("consumer priorRead={} localWriteCommit={}", priorRead,
		// localWriteCommit);

		if (isEmpty(localWriteCommit, localRead)) {
			localWriteCommit = writeCommit.getAcquire();			
			localRead = read.getAcquire();
			if (isEmpty(localWriteCommit, localRead)) {
				return null;
			}

		}

		final long next = localRead + 1;
		final int cyclicNext = getCyclicIndex(next);
		E e = array.get(cyclicNext);
		// logger.debug("consuming e={} at next={}", e, next);
		// array.setRelease(cyclicNext, null);
		read.setRelease(next);
		return e;
	}

	final private boolean isEmpty(long in, long out) {
		return count(in, out) == 0;

	}

	public boolean isFull() {
		return isFull(writeCommit.getAcquire(), read.getAcquire());
	}

	final private boolean isFull(long currentWrite, long currentRead) {
		// allow for stale values of currentRead
		return count(currentWrite, currentRead) >= capacity;
	}

	final private int count(long currentWrite, long currentRead) {
		if (currentWrite == INITIAL_INDEX)
			return 0;
		if (currentRead == INITIAL_INDEX) {
			return (int) currentWrite + 1;
		}

		return (int) (currentWrite - currentRead);
	}

	private int getCyclicIndex(long writeIndex) {
		return (int) writeIndex & mask;
	}

//	private boolean isEmptyCurried(long localReadCache) {
//		return isEmpty(writeCommit.getAcquire(), localReadCache);
//	}

	public boolean isEmpty() {
		return isEmpty(writeCommit.getAcquire(), read.getAcquire());
	}


	public void barriersDump() {
		System.out.println("consumerSignalBarrier " + consumerSignalBarrier.dump());
		System.out.println("producerSignalBarrier " + producerSignalBarrier.dump());

	}
}
