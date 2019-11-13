package ceki.ce.signal;

import java.util.concurrent.atomic.AtomicInteger;

public class BusyWaitSignalBarrier implements SignalBarier {

	final int maxYieldCount;
	volatile long parkCount;
	long signalCount;
	AtomicInteger sleepCount = new AtomicInteger(0);

	public BusyWaitSignalBarrier(int maxYieldCount) {
		this.maxYieldCount = maxYieldCount;

	}

	@Override
	public void signal() {
		signalCount++;
	}

	@Override
	public void await(int count) throws InterruptedException {
		parkCount++;
		if ((count & maxYieldCount) == maxYieldCount) {
			sleepCount.getAndIncrement();
			sleep(1);
		}
		Thread.yield();
	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String dump() {
		return " parkCount=" + parkCount + " sleepCount="+sleepCount+" signalCount="+signalCount;
	}
}
