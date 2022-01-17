package ceki.ce.signal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSignalBarrier implements SignalBarier {

	final String name;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	long parkCount;
	long signalCount;
	ConcurrentHashMap<Thread, Boolean> threadsMap = new ConcurrentHashMap<>();
	
	
	public DefaultSignalBarrier(String name) {
		this.name = name;
	}

	@Override
	public void parkNanos(long duration) throws InterruptedException {
		parkCount++;
		if((parkCount & 0x0FFF) == 0) {
		  logger.atDebug().addKeyValue("parkCount", parkCount).addKeyValue("threadsMapSize", threadsMap.size()).log("parkNanos called");
		}
		Thread currentThread = Thread.currentThread();
		threadsMap.put(currentThread, Boolean.TRUE);
		LockSupport.parkNanos(this, duration);
		threadsMap.remove(currentThread);
		if (currentThread.isInterrupted())
			throw new InterruptedException();
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
		return "DefaultSignalBarrier [name=" + name + ", parkCount=" + parkCount + ", signalCount=" + signalCount
				+ ", threadsMap.size=" + threadsMap.size() + "]";
	}



}
