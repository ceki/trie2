package ceki.ce;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CylicBufferTest {

	static Logger logger = LoggerFactory.getLogger(CylicBufferTest.class);

	int maxYield =  1024;
	int RUN_LENGTH = 10* 1000 * 1000;

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
	public void threaded() throws InterruptedException {

		List<Integer> results = new ArrayList<>();
		List<Integer> expected = new ArrayList<>();
		Runnable producerRunnable = new Runnable() {
			public void run() {
				for (int i = 0; i < RUN_LENGTH; i++) {
					int cycle = 0;
					while (true) {
						boolean success = ce.insert(i);
						if (success) {
							break;
						} else {
							if (cycle++ > maxYield) {
								cycle = 0;
								CylicBufferTest.sleep(10);
							}
							Thread.yield();
						}
					}
				}
				logger.info("Exiting producerRunnable");
			}

		};

		Runnable consumerRunnable = new Runnable() {
			public void run() {
				for (int i = 0; i < RUN_LENGTH; i++) {
					int cycle = 0;

					while (true) {
						Optional<Integer> result = ce.consume();
						if (result.isPresent()) {
							int r = result.get();
							if (r != i) {
								logger.warn("result = {} != expected = {} ", r, i);
							}
							logger.trace("adding {}", r);
							results.add(r);
							break;
						} else {;
							Thread.yield();
							if (cycle++ > maxYield) {
								cycle = 0;
								CylicBufferTest.sleep(10);
							}
						}
					}
				}
				logger.info("Exiting consumerRunnable");
			}
		};

		Thread consumer = new Thread(consumerRunnable);
		consumer.setName("*consumer");
		Thread producer = new Thread(producerRunnable);
		producer.setName(" producer");
		producer.start();
		consumer.start();

		producer.join();
		consumer.join();

//		for (int i = 0; i < RUN_LENGTH; i++) {
//			expected.add(i);
//		}
//
//		assertEquals(expected, results);

	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
