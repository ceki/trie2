package ceki.ce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CylicBufferTest {

	static Logger logger = LoggerFactory.getLogger(CylicBufferTest.class);

	static int MAX_YEILD_COUNT = 1024;
	static int TOTAL_RUN_LENGTH = 16 * 1024 * 1024 ;

	int capacity = 256;

	CylicBuffer<Integer> ce = new CylicBuffer<>(capacity);

	@Test
	public void smoke() {
		ce.insert(1);
		Optional<Integer> val = ce.consume();
		Integer expected = 1;
		assertEquals(expected, val.get());
	}

	@Test
	public void smokeInOut() {
		for (int i = 0; i <= capacity + 1; i++) {
			ce.insert(i);
			Optional<Integer> val = ce.consume();
			Integer expected = i;
			assertEquals(expected, val.get());
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
	
	class ProducerRunnable implements Runnable {
		
		final int id;
		final int totalProducers;
		
		ProducerRunnable(int id, int totalProducers) {
			this.id = id;
			this.totalProducers = totalProducers;
		}
		
		public void run() {
			int runLen = TOTAL_RUN_LENGTH/totalProducers;
			for (int i = 0; i < runLen; i++) {
				int cycle = 0;
				while (true) {
					boolean success = ce.insert(id+i*totalProducers);
					if (success) {
						break;
					} else {
						if (cycle++ > MAX_YEILD_COUNT) {
							cycle = 0;
							CylicBufferTest.sleep(1);
						}
						Thread.yield();
					}
				}
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
			for(int i = 0; i < totalProducers; i++)
				expected[i] = i;
		}
		
		public void run() {
			
			
			for (int i = 0; i < TOTAL_RUN_LENGTH; i++) {
				int cycle = 0;

				while (true) {
					Optional<Integer> result = ce.consume();
					if (result.isPresent()) {
						int r = result.get();
						
						int expectIndex = (r & totalProducersMask);
						int exp = expected[expectIndex];
						
						if (exp != r) {
							logger.warn("result = {} != expected = {} ", r, exp);
							fail();
						}
						expected[expectIndex] += totalProducers;
						break;
					} else {
						;
						Thread.yield();
						if (cycle++ > MAX_YEILD_COUNT) {
							cycle = 0;
							CylicBufferTest.sleep(10);
						}
					}
				}
			}
			logger.info("Exiting consumerRunnable");
		}
	};


	public void n_ProducersSingleConsumer(int totalProducers) throws InterruptedException {

		Thread[] producerThreads = new Thread[totalProducers];

		Runnable consumerRunnable = new ConsumerRunnable(totalProducers);
		Thread consumer = new Thread(consumerRunnable);
		consumer.setName("**consumer");
				
		for(int p = 0; p < totalProducers; p++) {
			producerThreads[p] = new Thread(new ProducerRunnable(p, totalProducers));
			producerThreads[p].setName("producer-"+p);
			producerThreads[p].start();
		}
		
		consumer.start();
		
		for(int p = 0; p < totalProducers; p++) {
			producerThreads[p].join();
		}			
		
		consumer.join();


	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
