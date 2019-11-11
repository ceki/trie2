package ceki.ce.signal;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class DefaultSignalBarrier implements SignalBarier {

	long parkCount;
	long signalCount;
	ConcurrentLinkedQueue<Thread> threadsQueue = new ConcurrentLinkedQueue<>();

	public DefaultSignalBarrier() {
	}

	@Override
	public void parkNanos(long duration) throws InterruptedException {
		parkCount++;
		Thread currentThread = Thread.currentThread();
		threadsQueue.add(currentThread);
		LockSupport.parkNanos(this, duration);
		if (currentThread.isInterrupted())
			throw new InterruptedException();
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

		threadsQueue.forEach(LockSupport::unpark);
	}

	@Override
	public String dump() {
		return " parkCount=" + parkCount + " signalCount=signalCount";
	}

}
