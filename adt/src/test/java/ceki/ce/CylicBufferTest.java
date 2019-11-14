package ceki.ce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ceki.ce.marked.MPSC_CylicBuffer;

public class CylicBufferTest {

	static Logger logger = LoggerFactory.getLogger(CylicBufferTest.class);

	static int TOTAL_RUN_LENGTH = 26_000_000;

	final int capacity = 1024*2;

	CylicBuffer<Integer> ce = new CylicBuffer<>(capacity, Integer.class);
	SPSCCylicBuffer<Integer> spscce = new SPSCCylicBuffer<>(capacity);
	final ABQ<Integer> abq = new ABQ<>(capacity, Integer.class);
	MPSC_CylicBuffer<Integer> mpsc = new MPSC_CylicBuffer<>(capacity);
	
	@Test
	public void smoke() {
		ce.put(1);
		Integer val = ce.take();
		Integer expected = 1;
		assertEquals(expected, val);
	}

	@Ignore
	@Test
	public void smokeABQ() {
		abq.put(1);
		Integer val = abq.take();
		Integer expected = 1;
		assertEquals(expected, val);
	}

	@Test
	public void smokeMPSC() {
		mpsc.put(1);
		Integer val = mpsc.take();
		Integer expected = 1;
		assertEquals(expected, val);
	}

	public void smokeInOut(ICylicBuffer<Integer> icb) {
		for (int i = 0; i <= capacity + 1; i++) {
			icb.put(i);
			Integer val = icb.take();
			if (val != null) {
				// System.out.println("valArray.length=" + valArray.length);
				//for (int j = 0; j < valArray.length; j++) {
					Integer expected = i;
					assertEquals(expected, val);
			} else {
				fail("empty return value");
			}
		}
	}

	
	@Ignore
	@Test
	public void smokeInOut() {
		smokeInOut(ce);
	}

	
	@Test
	public void abq_smokeInOut() {
		smokeInOut(abq);
	}
	
	@Test
	public void mpmc_smokeInOut() {
		smokeInOut(mpsc);
	}

	@Test
	public void mpsc_singleProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(mpsc, 1);
	}

	@Test
	public void mpsc_2ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(mpsc, 2);
	}
	
	@Test
	public void mpsc_4ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(mpsc, 4);
	}
	
	@Test
	public void mpsc_8ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(mpsc, 8);
	}
	
	
	@Ignore
	@Test
	public void noLock_singleProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 1);
	}

	@Ignore
	@Test
	public void spscce_singleProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(spscce, 1);
	}

	
	@Ignore
	@Test
	public void _noLock_singleProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 1);
	}

	
	@Ignore
	@Test
	public void noLock_twoProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 2);
	}
	
	@Ignore
	@Test
	public void noLock_4ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 4);
	}

	@Ignore
	@Test
	public void noLock_8ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 8);
	}

	@Ignore
	@Test
	public void nolock_32_ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 32);
	}

	@Ignore
	@Test
	public void noLock_64ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(ce, 64);
	}

	@Ignore
	@Test
	public void abq_singleProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 1);
	}

	@Ignore
	@Test
	public void abq_twoProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 2);
	}

	@Ignore
	@Test
	public void abq_4ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 4);
	}
	
	@Ignore
	@Test
	public void abq_32ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 32);
	}

	@Ignore
	@Test
	public void abq_64ProducerSingleConsumer() throws InterruptedException {
		n_ProducersSingleConsumer(abq, 64);
	}

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

		int expected[];
		public boolean failed = false;
		
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
				Integer value = icb.take();
				if (value != null) {
					totalConsumed ++; //= values.length;
					//validate(value);
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

	public void n_ProducersSingleConsumer(ICylicBuffer<Integer> icb, int totalProducers) throws InterruptedException {

		Thread[] producerThreads = new Thread[totalProducers];

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

		System.out.println("==========================================");
		System.out.println(icb.getClass() + "  totalProducers=" + totalProducers);
 		icb.barriersDump();

 		if(consumerRunnable.failed) {
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
