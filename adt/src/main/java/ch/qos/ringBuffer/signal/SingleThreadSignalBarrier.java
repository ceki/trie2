package ceki.ce.signal;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleThreadSignalBarrier implements SignalBarier {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	final static Integer ZERO = 0;
	
	final String name;

	long parkCount;
	long signalCount;
	final int maxYieldCount;
	AtomicReference<Thread> atomicWaitingThread = new AtomicReference<Thread>();
	
	ThreadLocal<Integer> cycleThreadLocal = new ThreadLocal<Integer>() {
		protected Integer initialValue() {
			return 0;
		}
	};
	
	public SingleThreadSignalBarrier(String name, int maxYieldCount) {
		this.name = name;
		this.maxYieldCount = maxYieldCount;
	}

 
	@Override
	public void parkNanos(long duration) throws InterruptedException {
		parkCount++;
		if ((parkCount & 0x00FF) == 0)
			logger.atDebug().addKeyValue("parkCount", parkCount).addKeyValue("signalCount", signalCount)
					.log("parkNanos called");
		
		int cycle = cycleThreadLocal.get();
		cycleThreadLocal.set(cycle+1);	
		if (cycle >= maxYieldCount) {
			cycleThreadLocal.set(ZERO);	
			
			Thread waitingThread = Thread.currentThread();
			boolean firstResult = atomicWaitingThread.compareAndSet(null, waitingThread);
			if (!firstResult)
				throw new IllegalStateException("Expecting null value");

			LockSupport.parkNanos(this, duration);
			boolean secondResult = atomicWaitingThread.compareAndSet(waitingThread, null);
			if (!secondResult)
				throw new IllegalStateException("Expecting previously set waitingThread");
		} else {
			Thread.yield();
		}
	}

	@Override
	public void signal() {
		signalCount++;
		if ((signalCount & 0x00FF) == 0)
			logger.atDebug().addKeyValue("parkCount", parkCount).addKeyValue("signalCount", signalCount)
					.log("signal called");
		Thread waitingThread = atomicWaitingThread.get();
		if (waitingThread != null)
			LockSupport.unpark(waitingThread);

	}


	@Override
	public String toString() {
		return "SingleThreadSignalBarrier [name=" + name + ", parkCount=" + parkCount
				+ ", signalCount=" + signalCount + ", cycle=" + cycleThreadLocal.get()  
				+ ", atomicWaitingThread=" + atomicWaitingThread.get() + "]";
	}
	
}
