/**
 * Copyright (c) 2019 QOS.ch Sarl
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package ch.qos.ringBuffer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.ringBuffer.signal.SignalBarrier;
import ch.qos.ringBuffer.signal.SignalBarrierFactory;

public class NullCheckingReaderRingBuffer<E> implements RingBuffer<E> {

//	static public class Value<E> {
//		AtomicReference<E> reference = new AtomicReference<E>(null);
//	}

	static Logger logger = LoggerFactory.getLogger(NullCheckingReaderRingBuffer.class);

	public final int capacity;
	public final int mask;

	final AtomicReferenceArray<AtomicReference<E>> array;
	
	// Given that the AtomicReferenceArray.compareAndExchange operation is based 
	// on == (identity equality), we need to know the indentities of the values
	// in advance
	final AtomicReference<E>[] referenceNodes;

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
		this.array = new AtomicReferenceArray<AtomicReference<E>>(capacity);

		this.referenceNodes = new AtomicReference[capacity];
		for (int i = 0; i < capacity; i++) {
			referenceNodes[i] = new AtomicReference<E>();
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

		final AtomicReference<E> referenceNode = referenceNodes[cyclicWriteSuccessor];
		referenceNode.setRelease(e); 

		array.set(cyclicWriteSuccessor, referenceNode);
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

		AtomicReference<E> referenceNode = referenceNodes[cyclicNext];

		// logger.debug("consumer reading at next={}", next);

		// Wait for the producer to finish writing
		while (true) {
			AtomicReference<E> n = array.compareAndExchange(cyclicNext, referenceNode, null);
			if (n == referenceNode) {
				E e = referenceNode.getAcquire();
				// logger.debug("consumer read e={} at next={} count={}", e, next, count);
				read.setRelease(next);
				return e;
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


	public boolean isEmpty() {
		return isEmpty(write.getAcquire(), read.getAcquire());
	}

	public void barriersDump() {
		System.out.println("consumerSignalBarrier " + consumerSignalBarrier.dump());
		System.out.println("producerSignalBarrier " + producerSignalBarrier.dump());

	}
}
