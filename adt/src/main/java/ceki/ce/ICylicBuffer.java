package ceki.ce;

import ceki.ce.signal.SignalBarier;

public interface ICylicBuffer<E> {

	E take();

	void put(E e);

	default void barriersDump() {
	}
	
	default void await(SignalBarier sb, int count) {
		try {
			sb.await(count);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
 