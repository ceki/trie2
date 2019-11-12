package ceki.ce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CylicBufferTest {

	static Logger logger = LoggerFactory.getLogger(CylicBufferTest.class);

	static int MAX_YEILD_COUNT = 2048;
	static int TOTAL_RUN_LENGTH = 20_000_000;

	int capacity = 256;

	CylicBuffer<Integer> ce = new CylicBuffer<>(capacity, Integer.class);
	final ABQ<Integer> abq = new ABQ<>(capacity, Integer.class);

	@Test
	public void smoke() {
		ce.insert(1);
		Optional<Integer[]> val = ce.consume();
		Integer expected = 1;
		assertEquals(expected, val.get()[0]);
	}

	@Test
	public void smokeABQ() {
		abq.put(1);
		Integer[] val = abq.take();
		Integer expected = 1;
		assertEquals(expected, val[0]);
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
	public void abq_smokeInOut() {
		for (int i = 0; i <= capacity + 1;) {
			abq.put(i);
			Integer[] valArray = abq.take();
			if(valArray != null) {
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
	public void noLock_singleProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 1);
	}

	@Test
	public void abq_singleProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 1);
	}

	@Test
	public void noLock_twoProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 2);
	}

	@Test
	public void abq_twoProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 2);
	}

	@Ignore
	@Test
	public void _8ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 8);
	}

	@Test
	public void nolock_32_ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 32);
	}

	@Test
	public void abq_32ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 32);
	}
	
	@Ignore
	@Test
	public void _64ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 64);
	}

	static final Integer ONE = new Integer(1);

	class ProducerRunnable implements Runnable {

		ICylicBuffer<Integer> icb;
		final int id;
		final int totalProducers;

		ProducerRunnable(ICylicBuffer<Integer> icb, int id, int totalProducers) {
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
		ICylicBuffer<Integer> icb;

		final int totalProducers;
		final int totalProducersMask;

		final int expected[];

		ConsumerRunnable(ICylicBuffer<Integer> icb, int totalProducers) {
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

				Integer[] values = icb.take();
				if (values != null) {
					totalConsumed += values.length;
					for (Integer v : values) {
						validate(v);
					}
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

	public void n_ProducersSingleConsumer(ICylicBuffer<Integer> icb, int totalProducers) throws InterruptedException {

		Thread[] producerThreads = new Thread[totalProducers];
		// SignalBarier consumerSignalBarrier = new
		// BusyWaitSignalBarrier(MAX_YEILD_COUNT);
		// SignalBarier consumerSignalBarrier = new DefaultSignalBarrier();

		ConsumerRunnable consumerRunnable = new ConsumerRunnable(icb, totalProducers);
		Thread consumer = new Thread(consumerRunnable);
		consumer.setName("**consumer");

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

//		System.out.println("==========================================");
//		System.out.println(
//				"totalProducers=" + totalProducers + " sum " + ce.sum + " avg=" + (ce.sum * 1.0 / ce.readCount));

		//ce.barriersDump();

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
