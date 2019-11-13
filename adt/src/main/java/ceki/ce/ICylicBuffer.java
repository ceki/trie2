package ceki.ce;

public interface ICylicBuffer<E> {

	E take();

	void put(E e);

	default void barriersDump() {
	}
}
