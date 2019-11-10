package ceki.ce.signal;

import java.util.concurrent.locks.LockSupport;

public class DefaultSignalBarrier implements SignalBarier {

	
	final Thread thread;
	long parkCount;
	long signalCount;
	
	public DefaultSignalBarrier(Thread thread) {
		this.thread = thread;
	}
	
	
	@Override
	public void parkNanos(long duration) throws InterruptedException {
		parkCount++;
		LockSupport.parkNanos(this, duration);
		if (thread.isInterrupted())
			throw new InterruptedException();
	}
	
	@Override
	public void signal() {
		signalCount++;
		LockSupport.unpark(thread);
	}


	@Override
	public String dump() {
		return " parkCount=" + parkCount + " signalCount=signalCount";
	}

}
