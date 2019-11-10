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

	static int MAX_YEILD_COUNT = 1024;
	static int TOTAL_RUN_LENGTH =  16 *1024*1024 ;

	int capacity = 256;

	CylicBuffer<Integer> ce = new CylicBuffer<>(capacity);

	@Test
	public void smoke() {
		ce.insert(1);
		Optional<List<Integer>> val = ce.consume();
		Integer expected = 1;
		assertEquals(expected, val.get().get(0));
	}
 
	@Test
	public void smokeInOut() {
		for (int i = 0; i <= capacity + 1; i++) {
			ce.insert(i);
			Optional<List<Integer>> val = ce.consume();
			Integer expected = i;
			assertEquals(expected, val.get().get(0));
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
		final SignalBarier consumerSignalBarrier;
		
		ProducerRunnable(int id, int totalProducers, SignalBarier consumerSignalBarrier ) {
			this.id = id;
			this.totalProducers = totalProducers;
			this.consumerSignalBarrier = consumerSignalBarrier;
		}
		
		public void run() {
			int runLen = TOTAL_RUN_LENGTH/totalProducers;
			for (int i = 0; i < runLen; i++) {
				int cycle = 0;
				while (true) {
					boolean empty = ce.isEmpty();
					boolean success = ce.insert(id+i*totalProducers);
					if (success) {
						if(empty)
							consumerSignalBarrier.signal();
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
		SignalBarier signalBarrier;
		
		final int expected[];
		
		ConsumerRunnable(int totalProducers) {
			this.totalProducers = totalProducers;
			this.totalProducersMask = totalProducers - 1;
			expected = new int[totalProducers];
			for(int i = 0; i < totalProducers; i++)
				expected[i] = i;
		}
		
		public void setSignalBarrier(SignalBarier signalBarrier) {
			this.signalBarrier = signalBarrier;
		}
		public void run() {
			
			int totalConsumed = 0;
			
			while(totalConsumed < TOTAL_RUN_LENGTH) {
				while (true) {
					Optional<List<Integer>> result = ce.consume();
					if (result.isPresent()) {
						
						List<Integer> rList = result.get();
						totalConsumed += rList.size();
						//rList.forEach(this::validate);
						//validate(r);
						
						break;
					} else {
						try {
							signalBarrier.parkNanos(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
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


	public void n_ProducersSingleConsumer(int totalProducers) throws InterruptedException {

		Thread[] producerThreads = new Thread[totalProducers];

		ConsumerRunnable consumerRunnable = new ConsumerRunnable(totalProducers);
		Thread consumer = new Thread(consumerRunnable);
		consumer.setName("**consumer");
		//SignalBarier signalBarrier = new BusyWaitSignalBarrier(MAX_YEILD_COUNT);
		//SignalBarier signalBarrier = new DefaultSignalBarrier(consumer);
		SignalBarier signalBarrier = new MixedSignalBarrier(MAX_YEILD_COUNT, consumer);
		
		consumerRunnable.setSignalBarrier(signalBarrier);
				
		for(int p = 0; p < totalProducers; p++) {
			producerThreads[p] = new Thread(new ProducerRunnable(p, totalProducers, signalBarrier));
			producerThreads[p].setName("producer-"+p);
			producerThreads[p].start();
		}
		
		consumer.start();
		
		for(int p = 0; p < totalProducers; p++) {
			producerThreads[p].join();
		}			
		
		consumer.join();
		
		System.out.println("signalBarrier "+signalBarrier.dump());
		
	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
