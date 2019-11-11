package ceki.ce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ceki.ce.signal.BusyWaitSignalBarrier;
import ceki.ce.signal.DefaultSignalBarrier;
import ceki.ce.signal.MixedSignalBarrier;
import ceki.ce.signal.SignalBarier;

public class CylicBufferTest {

	static Logger logger = LoggerFactory.getLogger(CylicBufferTest.class);

	static int MAX_YEILD_COUNT = 2048;
	static int TOTAL_RUN_LENGTH = 16 * 1024 * 1024;

	int capacity = 256;

	CylicBuffer<Integer> ce = new CylicBuffer<>(capacity, Integer.class);

	@Test
	public void smoke() {
		ce.insert(1);
		Optional<Integer[]> val = ce.consume();
		Integer expected = 1;
		assertEquals(expected, val.get()[0]);
	}

	@Test
	public void smokeInOut() {
		for (int i = 0; i <= capacity + 1;) {
			ce.insert(i);
			Optional<Integer[]> val = ce.consume();
			if (val.isPresent()) {
				Integer[] valArray = val.get();
				// System.out.println("valArray.length=" + valArray.length);
				for (int j = 0; j < valArray.length; j++) {
					Integer expected = i + j;
					assertEquals(expected, valArray[j]);
				}
				i += valArray.length;
			} else {
				fail("empty return value");
			}
		}
	}

	@Test
	public void singleProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(1);
	}

	@Test
	public void twoProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(2);
	}

	@Test
	public void _16ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(64);
	}

	static final Integer ONE = new Integer(1);

	class ProducerRunnable implements Runnable {

		final int id;
		final int totalProducers;

		ProducerRunnable(int id, int totalProducers) {
			this.id = id;
			this.totalProducers = totalProducers;
		}

		public void run() {
			int runLen = TOTAL_RUN_LENGTH / totalProducers;
			for (int i = 0; i < runLen; i++) {
				ce.put(id + i * totalProducers);
			}
			logger.info("Exiting producerRunnable");
		}
	};

	class ConsumerRunnable implements Runnable {
		final int totalProducers;
		final int totalProducersMask;

		final int expected[];

		ConsumerRunnable(int totalProducers) {
			this.totalProducers = totalProducers;
			this.totalProducersMask = totalProducers - 1;
			expected = new int[totalProducers];
			for (int i = 0; i < totalProducers; i++)
				expected[i] = i;
		}

		public void run() {
			int totalConsumed = 0;
			while (totalConsumed < TOTAL_RUN_LENGTH) {

				Integer[] values = ce.take();
				totalConsumed += values.length;
				for (Integer v : values) {
					validate(v);
				}
			}
			logger.info("Exiting consumerRunnable");
		}

		private void validate(int r) {
			int expectIndex = (r & totalProducersMask);
			int exp = expected[expectIndex];

			if (exp != r) {
				logger.warn("result = {} != expected = {} ", r, exp);
				fail();
			}
			expected[expectIndex] += totalProducers;
		}

	};

	public void n_ProducersSingleConsumer(int totalProducers) throws InterruptedException {

		Thread[] producerThreads = new Thread[totalProducers];
		// SignalBarier consumerSignalBarrier = new
		// BusyWaitSignalBarrier(MAX_YEILD_COUNT);
		// SignalBarier consumerSignalBarrier = new DefaultSignalBarrier();
	
		ConsumerRunnable consumerRunnable = new ConsumerRunnable(totalProducers);
		Thread consumer = new Thread(consumerRunnable);
		consumer.setName("**consumer");

		for (int p = 0; p < totalProducers; p++) {
			producerThreads[p] = new Thread(
					new ProducerRunnable(p, totalProducers));
			producerThreads[p].setName("producer-" + p);
			producerThreads[p].start();
		}

		consumer.start();

		for (int p = 0; p < totalProducers; p++) {
			producerThreads[p].join();
		}

		consumer.join();

		System.out.println(
				"totalProducers=" + totalProducers + " sum " + ce.sum + " avg=" + (ce.sum * 1.0 / ce.readCount));
	
		ce.barriersDump();
	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
