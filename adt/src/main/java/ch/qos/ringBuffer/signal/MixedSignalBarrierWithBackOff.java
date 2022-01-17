/**
 * Copyright (c) 2019 QOS.ch Sarl
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package ch.qos.ringBuffer.signal;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public class MixedSignalBarrierWithBackOff implements SignalBarrier {

	final int maxYieldCount;
	final int duration;

	int parkedCount;
	int unparkCount = 0;
	int totalAwaitCalls = 0;

	ConcurrentLinkedQueue<Thread> threadsQueue = new ConcurrentLinkedQueue<>();

	public MixedSignalBarrierWithBackOff(int maxYieldCount, int duration) {
		this.maxYieldCount = maxYieldCount;
		this.duration = duration;
	}

	@Override
	public void await(int count) throws InterruptedException {
		
		totalAwaitCalls++;
		if ( count > maxYieldCount) {
			parkedCount++;
			Thread currentThread = Thread.currentThread();
			threadsQueue.add(currentThread);
			LockSupport.parkNanos(this, duration);

			if (currentThread.isInterrupted())
				throw new InterruptedException();
		} else {
			int bitCount = BitUtil.leftMostBit(count) + 1;
			//if(count> 0)
				//System.out.println(Thread.currentThread().getName()+ " count="+count+" bitCount="+bitCount);
			for (int i = 0; i < bitCount; i++)
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
		// return "";
		return "totalAwaitCalls=" + totalAwaitCalls + " parkedCount=" + parkedCount + " unparkCount=" + unparkCount;
	}

	static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
