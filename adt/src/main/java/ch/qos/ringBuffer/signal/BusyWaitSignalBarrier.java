package ch.qos.ringBuffer.signal;

public class BusyWaitSignalBarrier implements SignalBarrier {

	long signalCount;

	int totalAwaitCalls = 0;
	
	public BusyWaitSignalBarrier() {

	}

	@Override
	public void signal() {
	}

	@Override
	public void await(int count) throws InterruptedException {
		totalAwaitCalls++;
		Thread.yield();
	}

	
	

	@Override
	public String dump() {
		return "totalAwaitCalls=" + totalAwaitCalls;
	}
}