package ceki.ce.signal;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class MixedSignalBarrier implements SignalBarier {

	final int maxYieldCount;
	final int duration;
	
	int parkedCount;
	int unparkCount = 0;
	volatile int totalAwaitCalls  = 0;
	
	ConcurrentLinkedQueue<Thread> threadsQueue = new ConcurrentLinkedQueue<>();

	public MixedSignalBarrier(int maxYieldCount, int duration) {
		this.maxYieldCount = maxYieldCount;
		this.duration = duration;
	}

	@Override
	public void await(int count) throws InterruptedException {
		totalAwaitCalls++;
		if ((count & maxYieldCount) == maxYieldCount) {
			Thread currentThread = Thread.currentThread();
			threadsQueue.add(currentThread);
			LockSupport.parkNanos(this, duration);

			if (currentThread.isInterrupted())
				throw new InterruptedException();
		} else {
			Thread.yield();
		}
	}

	@Override
	public void signal() {
		
		while (true) {
			Thread t = threadsQueue.poll();
			if (t == null)
				break;
			else {
				unparkCount++;
				LockSupport.unpark(t);					
			}
		}
	}

	@Override
	public String dump() {
		//return "";
		return "totalAwaitCalls="+totalAwaitCalls+" parkedCount=" + parkedCount + " unparkCount=" + unparkCount;
	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
