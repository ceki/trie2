package ceki.ce.signal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixedSignalBarrier implements SignalBarier {

	final static Integer ZERO = 0;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	final String name;
	final int maxYieldCount;
	int busyWait;
	int parkedCount;
	private int signalCount = 0;

	ConcurrentHashMap<Thread, Boolean> threadsMap = new ConcurrentHashMap<>();
	
	ThreadLocal<Integer> cycleThreadLocal = new ThreadLocal<Integer>() {
		protected Integer initialValue() {
			return 0;
		}
	};
	
	//int cycle = 0;

	public MixedSignalBarrier(String name, int maxYieldCount) {
		this.name = name;
		this.maxYieldCount = maxYieldCount;
	}

	
	@Override
	public void parkNanos(long duration) throws InterruptedException {
		parkedCount++;

		int cycle = cycleThreadLocal.get();
		cycleThreadLocal.set(cycle+1);	
		if (cycle >= maxYieldCount) {
			cycleThreadLocal.set(ZERO);	
			
			Thread currentThread = Thread.currentThread();
			threadsMap.put(currentThread, Boolean.TRUE);
			LockSupport.parkNanos(this, duration);
			threadsMap.remove(currentThread);
			if (currentThread.isInterrupted())
				throw new InterruptedException();
		} else {
			busyWait++;
			Thread.yield();
		}
	}

	@Override
	public void signal() {
		signalCount++;
		if((signalCount & 0x0FFF) == 0) {
			  logger.atDebug().addKeyValue("signalCount", signalCount).addKeyValue("threadsMapSize", threadsMap.size()).log("signal() called");
		}
		//System.out.println("signal() called. "+this + " " + Thread.currentThread());
		for(Thread t: threadsMap.keySet()) {
			LockSupport.unpark(t);
		}
	}

	@Override
	public String toString() {
		return "MixedSignalBarrier [maxYieldCount=" + maxYieldCount + ", busyWait=" + busyWait + ", parkedCount="
				+ parkedCount + ", signalCount=" + signalCount + ", threadsMap.size=" + threadsMap.size() + ", cycle=" + cycleThreadLocal.get()
				+ "]";
	}

}
