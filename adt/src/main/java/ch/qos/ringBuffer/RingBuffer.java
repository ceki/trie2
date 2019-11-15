package ch.qos.ringBuffer;

import ch.qos.ringBuffer.signal.SignalBarrier;


/**
 * 
 * A minimalistic interface for a ring buffer where callers can
 * {@link #put(E)} and then {@link #take()} data of type E.
 * 
 * <p>Callers of the {@link #put(E)} method are producers. Callers of 
 * the {@link #take()} method are consumers.
 * 
 * <p>A consumer will wait for data to be available. In case the 
 * underlying buffer is full, then producers wait until space is 
 * made available by the consumer.
 * 
 * @param <E>
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public interface RingBuffer<E> {

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
 