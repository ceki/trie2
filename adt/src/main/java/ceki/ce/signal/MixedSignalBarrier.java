package ceki.ce.signal;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class MixedSignalBarrier implements SignalBarier {

	final int maxYieldCount;
	int busyWait;
	int parkedCount;
	private int unparkCount = 0;

	ConcurrentLinkedQueue<Thread> threadsQueue = new ConcurrentLinkedQueue<>();

	
	int cycle = 0;

	public MixedSignalBarrier(int maxYieldCount) {
		this.maxYieldCount = maxYieldCount;
	}

	@Override
	public void parkNanos(long duration) throws InterruptedException {
		busyWait++;

		if (cycle++ > maxYieldCount) {
			cycle = 0;
			parkedCount++;
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
		return " busyWait=" + busyWait + " parkedCount=" + parkedCount + " unparkCount=" + unparkCount;
	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
