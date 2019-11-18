package ch.qos.ringBuffer;

import org.jctools.queues.MpscArrayQueue;

import ch.qos.ringBuffer.signal.SignalBarrier;
import ch.qos.ringBuffer.signal.SignalBarrierFactory;

public class JCToolsRB<E> implements RingBuffer<E>{

	
	SignalBarrier consumerSignalBarrier = SignalBarrierFactory.makeSignalBarrier();
	SignalBarrier producerSignalBarrier = SignalBarrierFactory.makeSignalBarrier();
	MpscArrayQueue<E> queue;
	
	JCToolsRB(int capacity) {
		queue = new MpscArrayQueue<>(capacity);
	}
	
	
	@Override
	public void put(E e) {

		while (true) {
			boolean success = queue.offer(e);
			if (success) {
				break;
			} else {
				try {
					producerSignalBarrier.await(0);
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
			E result = this.queue.poll();
			if (result != null) {
				return result;
			} else {
				await(consumerSignalBarrier, lastTakeCount);
			}
		}
	}

	

	
}
