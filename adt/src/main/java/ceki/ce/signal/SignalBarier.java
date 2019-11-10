package ceki.ce.signal;

public interface SignalBarier {

	void signal();

	void parkNanos(long duration) throws InterruptedException;

	String dump();

}