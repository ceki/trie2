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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.ringBuffer.ABQ;
import ch.qos.ringBuffer.DoubleWriterLockedRingBuffer;
import ch.qos.ringBuffer.RingBuffer;
import ch.qos.ringBuffer.SingleProducerSingleConsumerRingBuffer;

public class CylicBufferTest {

	static Logger logger = LoggerFactory.getLogger(CylicBufferTest.class);

	static int TOTAL_RUN_LENGTH = 10_000_000;

	final int capacity = 1024 * 2;

	DoubleWriterLockedRingBuffer<Integer> doubleWriterLockedRB = new DoubleWriterLockedRingBuffer<>(capacity, Integer.class);
	SingleProducerSingleConsumerRingBuffer<Integer> spscRB = new SingleProducerSingleConsumerRingBuffer<>(capacity);
	final ABQ<Integer> abq = new ABQ<>(capacity, Integer.class);
	NullCheckingReaderRingBuffer<Integer> nullCheckingRB = new NullCheckingReaderRingBuffer<>(capacity);
	JCToolsRB<Integer> jctoolsRB = new JCToolsRB<>(capacity);

	@Test
	public void smoke() {
		doubleWriterLockedRB.put(1);
		Integer val = doubleWriterLockedRB.take();
		Integer expected = 1;
		assertEquals(expected, val);
	}

	// @Ignore
	@Test
	public void smokeABQ() {
		abq.put(1);
		Integer val = abq.take();
		Integer expected = 1;
		assertEquals(expected, val);
	}

	@Test
	public void smokeMPSC() {
		nullCheckingRB.put(1);
		Integer val = nullCheckingRB.take();
		Integer expected = 1;
		assertEquals(expected, val);
	}

	public void smokeInOut(RingBuffer<Integer> icb) {
		for (int i = 0; i <= capacity + 1; i++) {
			icb.put(i);
			Integer val = icb.take();
			if (val != null) {
				// System.out.println("valArray.length=" + valArray.length);
				// for (int j = 0; j < valArray.length; j++) {
				Integer expected = i;
				assertEquals(expected, val);
			} else {
				fail("empty return value");
			}
		}
	}

	// @Ignore
	@Test
	public void smokeInOut() {
		smokeInOut(doubleWriterLockedRB);
	}

	@Test
	public void abq_smokeInOut() {
		smokeInOut(abq);
	}

	@Test
	public void mpmc_smokeInOut() {
		smokeInOut(nullCheckingRB);
	}

	@Ignore
	@Test
	public void nullCheckingRB_1() throws InterruptedException {
		n_ProducersSingleConsumer(nullCheckingRB, 1);
	}

	@Ignore
	@Test
	public void doubleWriterLockedRB() throws InterruptedException {
		n_ProducersSingleConsumer(doubleWriterLockedRB, 1);
	}


	@Ignore
	@Test
	public void abq1() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 1);
	}


	@Test
	public void all() {

		//RingBuffer<Integer> icbArray[] = new RingBuffer[] { ce, spscce, mpsc, abq };
		RingBuffer<Integer> icbArray[] = new RingBuffer[] { nullCheckingRB, jctoolsRB };

		
		for (RingBuffer<Integer> icq : icbArray) {
			for (int numProducers = 1; numProducers <= 128; numProducers *= 2) {
				try {
					n_ProducersSingleConsumer(icq, numProducers);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	class ProducerRunnable implements Runnable {

		RingBuffer<Integer> icb;
		final int id;
		final int totalProducers;

		ProducerRunnable(RingBuffer<Integer> icb, int id, int totalProducers) {
			this.icb = icb;
			this.id = id;
			this.totalProducers = totalProducers;
		}

		public void run() {
			int runLen = TOTAL_RUN_LENGTH / totalProducers;
			for (int i = 0; i < runLen; i++) {
				icb.put(id + i * totalProducers);
			}
			logger.info("Exiting producerRunnable");
		}
	};

	class ConsumerRunnable implements Runnable {
		RingBuffer<Integer> icb;

		final int totalProducers;
		final int totalProducersMask;

		int expected[];
		public boolean failed = false;

		ConsumerRunnable(RingBuffer<Integer> icb, int totalProducers) {
			this.icb = icb;
			this.totalProducers = totalProducers;
			this.totalProducersMask = totalProducers - 1;
			expected = new int[totalProducers];
			for (int i = 0; i < totalProducers; i++)
				expected[i] = i;
		}

		public void run() {
			int totalConsumed = 0;
			while (totalConsumed < TOTAL_RUN_LENGTH) {
				Integer value = icb.take();
				if (value != null) {
					totalConsumed++; // = values.length;
					validate(value);
				}
			}
			logger.info("Exiting consumerRunnable");
		}

		private void validate(int r) {
			int expectIndex = (r & totalProducersMask);
			int exp = expected[expectIndex];

			if (exp != r) {
				logger.error("result = {} != expected = {} ", r, exp);
				failed = true;
			}
			expected[expectIndex] += totalProducers;
		}

	};

	public void n_ProducersSingleConsumer(RingBuffer<Integer> icb, int totalProducers) throws InterruptedException {

		Thread[] producerThreads = new Thread[totalProducers];

		ConsumerRunnable consumerRunnable = new ConsumerRunnable(icb, totalProducers);
		Thread consumer = new Thread(consumerRunnable);
		consumer.setName("**consumer");

		long start = System.currentTimeMillis();

		for (int p = 0; p < totalProducers; p++) {
			producerThreads[p] = new Thread(new ProducerRunnable(icb, p, totalProducers));
			producerThreads[p].setName("producer-" + p);
			producerThreads[p].start();
		}

		consumer.start();

		for (int p = 0; p < totalProducers; p++) {
			producerThreads[p].join();
		}

		consumer.join();

		long end = System.currentTimeMillis();

		//System.out.println("==========================================");

		double diff = (end - start);

		double millionOpsPerSec = (1.0d * TOTAL_RUN_LENGTH / diff) / 1_000;
		String millionOpsPerSecStr = String.format(Locale.US, "%.2f", millionOpsPerSec);

		System.out.println(
				icb.getClass() + "  totalProducers=" + totalProducers + " opsPerSecStr=" + millionOpsPerSecStr);
		//icb.barriersDump();

		if (consumerRunnable.failed) {
			fail();
		}

		// dumpExpected(consumerRunnable.expected);
	}

	private void dumpExpected(int[] expected) {

		for (int i = 0; i < expected.length; i++) {
			System.out.println("expected[" + i + "]=" + expected[i]);
		}

	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
