package ch.qos.ringBuffer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.ringBuffer.signal.BusyWaitSignalBarrier;
import ch.qos.ringBuffer.signal.SignalBarrier;
import ch.qos.ringBuffer.signal.SignalBarrierFactory;

public class SPSCCylicBuffer<E> implements ICylicBuffer<E> {

	static Logger logger = LoggerFactory.getLogger(SPSCCylicBuffer.class);

	public final int capacity;
	public final int mask;

	final AtomicReferenceArray<E> array;

	static final int MAX_YEILD_COUNT = 1;
	static final int PARK_DURATION = 1;

	SignalBarrier consumerSignalBarrier = SignalBarrierFactory.makeSignalBarrier();
	SignalBarrier producerSignalBarrier =  SignalBarrierFactory.makeSignalBarrier();

	static final long INITIAL_INDEX = -1;
	AtomicLong write = new AtomicLong(INITIAL_INDEX);
	long writeCache = INITIAL_INDEX;
	AtomicLong read = new AtomicLong(INITIAL_INDEX);
	long readCache = INITIAL_INDEX;

	SPSCCylicBuffer(int capacity) {
		this.capacity = capacity;
		this.mask = capacity - 1;
		this.array = new AtomicReferenceArray<E>(capacity);
	}

	int lastPutCount = 0;

	@Override
	public void put(E e) {

		while (true) {
			boolean success = this.insert(e);
			if (success) {
				break;
			} else {
				try {
					producerSignalBarrier.await(lastPutCount);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	int lastTakeCount = 0;

	@Override
	public E take() {
		while (true) {
			E result = this.consume();
			if (result != null) {
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

	// ==========================================================================

	private boolean insert(E e) {
		long localWrite = write.getAcquire();
		long localRead = readCache;

		if (isFull(localWrite, localRead)) {
			readCache = read.getAcquire();
			localRead = readCache;
			if (isFull(localWrite, localRead)) {
				return false;
			}

		}

		final long writeSuccessor = localWrite + 1;
		final int cyclicWriteSuccessor = getCyclicIndex(writeSuccessor);

		// logger.debug("inserted e={} at next={}", e, writeSuccessor);

		array.setRelease(cyclicWriteSuccessor, e);
		write.setRelease(writeSuccessor);

		return true;

	}

	private E consume() {

		long localRead = read.getAcquire();

		long localWrite = writeCache;

		// logger.debug("consumer priorRead={} localWriteCommit={}", priorRead,
		// localWriteCommit);

		if (isEmpty(localWrite, localRead)) {
			writeCache = write.getAcquire();;
			localWrite = writeCache;
			if (isEmpty(localWrite, localRead)) {
				return null;
			}
		}

		final long next = localRead + 1;
		final int cyclicNext = getCyclicIndex(next);
		E e = array.getAcquire(cyclicNext);
		// logger.debug("consuming e={} at next={}", e, next);
		// array.setRelease(cyclicNext, null);
		read.setRelease(next);
		return e;
	}

	final private boolean isEmpty(long in, long out) {
		return count(in, out) == 0;

	}

	public boolean isFull() {
		return isFull(write.getAcquire(), read.getAcquire());
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
		return isEmpty(write.getAcquire(), read.getAcquire());
	}

	public void barriersDump() {
		System.out.println("consumerSignalBarrier " + consumerSignalBarrier.dump());
		System.out.println("producerSignalBarrier " + producerSignalBarrier.dump());

	}
}
