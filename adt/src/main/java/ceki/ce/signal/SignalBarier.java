package ceki.ce.signal;

public interface SignalBarier {

	void signal();

	void await(int count) throws InterruptedException;

	String dump();

}