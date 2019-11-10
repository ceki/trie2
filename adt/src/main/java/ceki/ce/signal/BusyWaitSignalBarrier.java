package ceki.ce.signal;

public class BusyWaitSignalBarrier implements SignalBarier {

	final int maxYieldCount;
	long parkCount;
	long signalCount;
	long heavyParkedCount;
	int cycle = 0;

	public BusyWaitSignalBarrier(int maxYieldCount) {
		this.maxYieldCount = maxYieldCount;

	}

	@Override
	public void signal() {
		signalCount++;
	}

	@Override
	public void parkNanos(long duration) throws InterruptedException {
		parkCount++;
		if (cycle++ > maxYieldCount) {
			cycle = 0;
			heavyParkedCount++;
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
		return " parkCount=" + parkCount + " heavyParkedCount="+heavyParkedCount+" signalCount="+signalCount;
	}
}
