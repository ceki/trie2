package ch.qos.ringBuffer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.ringBuffer.signal.SignalBarrier;
import ch.qos.ringBuffer.signal.SignalBarrierFactory;

public class NullCheckingReaderRingBuffer<E> implements RingBuffer<E> {

	static public class Value<E> {

		volatile E e;

		public Value(E e) {
			this.e = e;
		}

		public Value() {
			this.e = null;
		}

	}

	static Logger logger = LoggerFactory.getLogger(NullCheckingReaderRingBuffer.class);

	public final int capacity;
	public final int mask;

	final AtomicReferenceArray<Value<E>> array;
	final Value<E>[] valueNodes;

	static final int MAX_YEILD_COUNT = 1;
	static final int PARK_DURATION = 1;

	SignalBarrier consumerSignalBarrier = SignalBarrierFactory.makeSignalBarrier();
	SignalBarrier producerSignalBarrier = SignalBarrierFactory.makeSignalBarrier();

	static final long INITIAL_INDEX = -1;
	AtomicLong write = new AtomicLong(INITIAL_INDEX);
	AtomicLong read = new AtomicLong(INITIAL_INDEX);

	@SuppressWarnings("unchecked")
	public NullCheckingReaderRingBuffer(int capacity) {
		this.capacity = capacity;
		this.mask = capacity - 1;
		this.array = new AtomicReferenceArray<Value<E>>(capacity);

		this.valueNodes = new Value[capacity];
		for (int i = 0; i < capacity; i++) {
			valueNodes[i] = new Value<E>();
			this.array.set(i, null);
		}

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

	int lastTakeCount = 1;

	@Override
	public E take() {

		while (true) {
			E result = this.consume();
			if (result != null) {
				return result;
			} else {
				await(consumerSignalBarrier, lastTakeCount);
			}
		}
	}

	private boolean insert(E e) {
		long localWrite;
		long localRead = read.getAcquire();

		while (true) {
			localWrite = write.getAcquire();

			if (isFull(localWrite, localRead)) {
				return false;
			}

			// reserve the write index at old reserved value + 1
			boolean success = write.compareAndSet(localWrite, localWrite + 1);
			if (success) {
				break;
			}
		}

		final long writeSuccessor = localWrite + 1;
		final int cyclicWriteSuccessor = getCyclicIndex(writeSuccessor);

		// logger.debug("inserted e={} at next={}", e, writeSuccessor);

		// Empty<E> emptyNode = emptyNodes[cyclicWriteSuccessor];
		final Value<E> valueNode = valueNodes[cyclicWriteSuccessor];
		valueNode.e = e;

		array.set(cyclicWriteSuccessor, valueNode);
		return true;

	}

	private E consume() {

		long localRead = read.getAcquire();

		long localWrite = write.getAcquire();

		// logger.debug("consumer localRead={} localWrite={}", localRead, localWrite);

		if (isEmpty(localWrite, localRead)) {
			return null;
		}

		final long next = localRead + 1;
		final int cyclicNext = getCyclicIndex(next);

		Value<E> valueNode = valueNodes[cyclicNext];

		// logger.debug("consumer reading at next={}", next);

		while (true) {
			Value<E> n = array.compareAndExchange(cyclicNext, valueNode, null);
			if (n == valueNode) {
				E e = valueNode.e;
				// logger.debug("consumer read e={} at next={} count={}", e, next, count);
				read.setRelease(next);
				return e;
			} else {
				// await(consumerSignalBarrier, 0);
			}
		}
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

	final private int getCyclicIndex(long writeIndex) {
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
