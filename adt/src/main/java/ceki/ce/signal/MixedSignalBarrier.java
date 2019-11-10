package ceki.ce.signal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport; 

public class MixedSignalBarrier implements SignalBarier {

	final int maxYieldCount;
	final Thread thread;
	long parkCount;
	long signalCount;
	long heavyParkedCount;
	private int heavySignalCount = 0;
		
	AtomicBoolean parked = new AtomicBoolean(Boolean.FALSE);
	
	int cycle = 0;

	public MixedSignalBarrier(int maxYieldCount, Thread thread) {
		this.thread = thread;
		this.maxYieldCount = maxYieldCount;
	}
	
	
	@Override
	public void parkNanos(long duration) throws InterruptedException {
		parkCount++;

		if (cycle++ > maxYieldCount) {
			cycle = 0;
			heavyParkedCount++;
			parked.set(Boolean.TRUE);
			LockSupport.parkNanos(this, duration);
			
			if (thread.isInterrupted())
				throw new InterruptedException();
		}
		Thread.yield();
		
	}
	
	@Override
	public void signal() {
		signalCount++;
		
		if(parked.compareAndSet(Boolean.TRUE, Boolean.FALSE)) {
			heavySignalCount ++;
			LockSupport.unpark(thread);
		}
	}

	@Override 
	public String dump() {
		return " parkCount=" + parkCount + " heavyParked="+heavyParkedCount+" signalCount="+signalCount+ " heavySignalCount="+heavySignalCount;
	}
	
	
	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
