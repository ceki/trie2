package ceki.ce;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CylicBufferTest {

	
	int size = 4;
	CylicBuffer<Integer> ce = new CylicBuffer<>(size);
	

	
	@Test
	public void smoke() {
		ce.insert(1);
		Integer val = ce.consume();
		Integer expected= 1;
		assertEquals(expected, val);
	}
	
	
	@Test
	public void smokeInOut() {
		for(int i = 0; i <= size + 1 ; i++) {
			ce.insert(i);
			Integer val = ce.consume();
			Integer expected = i;
			assertEquals(expected, val);
		}
	}

	@Test
	public void threaded() throws InterruptedException {
		
		int RUN_LENGTH = 100;
		
		List<Integer> results = new ArrayList<>();
		List<Integer> expected = new ArrayList<>();
		Runnable producerRunnable = new Runnable() {
			public void run() {
				for(int i = 0; i < RUN_LENGTH; i++) {
					ce.insert(i);
				}
			}
		};
		
		Runnable consumerRunnable = new Runnable() {
			public void run() {
				for(int i = 0; i < RUN_LENGTH; i++) {
					Integer r = ce.consume();
					System.out.println("adding "+ r);
					results.add(r);
				}
			}
		};
		
		Thread consumer = new Thread(consumerRunnable);
		consumer.setName("consumer");
		Thread producer = new Thread(producerRunnable);
		producer.setName("producer");
		producer.start();
		consumer.start();
		
	
		producer.join();
		consumer.join();

		for(int i = 0; i < RUN_LENGTH; i++) {
			expected.add(i);
		}
		
		assertEquals(expected, results);

	}
}
