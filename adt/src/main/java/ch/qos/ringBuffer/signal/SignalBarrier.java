package ch.qos.ringBuffer.signal;

public interface SignalBarrier {

	void signal();

	void await(int count) throws InterruptedException;

	String dump();

}