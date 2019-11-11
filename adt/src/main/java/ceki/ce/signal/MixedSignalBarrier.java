package ceki.ce.signal;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport; 

public class MixedSignalBarrier implements SignalBarier {

	final int maxYieldCount;
	long parkCount;
	long signalCount;
	long heavyParkedCount;
	private int heavySignalCount = 0;
		
	ConcurrentLinkedQueue<Thread> threadsQueue = new ConcurrentLinkedQueue<>();
	
	int cycle = 0;

	public MixedSignalBarrier(int maxYieldCount) {
		this.maxYieldCount = maxYieldCount;
	}
	
	
	@Override
	public void parkNanos(long duration) throws InterruptedException {
		parkCount++;

		if (cycle++ > maxYieldCount) {
			cycle = 0;
			heavyParkedCount++;
			Thread currentThread = Thread.currentThread();
			threadsQueue.add(currentThread);
			LockSupport.parkNanos(this, duration);
			
			if (currentThread.isInterrupted())
				throw new InterruptedException();
		}
		Thread.yield();
		
	}
	
	@Override
	public void signal() {
		signalCount++;
		
		while (true) {
			Thread t = threadsQueue.poll();
			LockSupport.unpark(t);
			if (t == null)
				break;
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
