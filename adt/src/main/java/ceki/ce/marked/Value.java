package ceki.ce.marked;

public class Value<E> implements Node<E> {

	volatile E e;

	public Value(E e) {
		this.e = e;
	}
	
	public Value() {
		this.e = null;
	}
	
}
