package ch.qos.ringBuffer;

import java.util.concurrent.ArrayBlockingQueue;


/**
 * 
 * An implementation of {@link RingBuffer} interface based
 * on {@link ArrayBlockingQueue} to serve as a point of comparison.
 * 
 * @author ceki
 *
 * @param <E>
 */
public class ABQ<E> implements RingBuffer<E> {

	int capacity;

	final ArrayBlockingQueue<E> abq;
	Class<E> clazz;

	ABQ(int capacity, Class<E> clazz) {
		this.clazz = clazz;
		this.capacity = capacity;

		abq = new ArrayBlockingQueue<>(capacity);
	}

	@Override
	public E take() {
		try {
			E e = abq.take();
			return e;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return null;
		}

	}

	@Override
	public void put(E e) {
		try {
			abq.put(e);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

	}

}
