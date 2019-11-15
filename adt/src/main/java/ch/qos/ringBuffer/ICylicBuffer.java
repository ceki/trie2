package ch.qos.ringBuffer;

import ch.qos.ringBuffer.signal.SignalBarrier;

public interface ICylicBuffer<E> {

	E take();

	void put(E e);

	default void barriersDump() {
	}
	
	default void await(SignalBarrier sb, int count) {
		try {
			sb.await(count);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
 