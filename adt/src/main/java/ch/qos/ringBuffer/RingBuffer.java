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

package ch.qos.ringBuffer;

import ch.qos.ringBuffer.signal.SignalBarrier;


/**
 * 
 * A minimalistic interface for a ring buffer where callers can
 * {@link #put(E)} and then {@link #take()} data of type E.
 * 
 * <p>Callers of the {@link #put(E)} method are producers. Callers of 
 * the {@link #take()} method are consumers.
 * 
 * <p>A consumer will wait for data to be available. In case the 
 * underlying buffer is full, then producers wait until space is 
 * made available by the consumer.
 * 
 * @param <E>
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public interface RingBuffer<E> {

	E take();

	void put(E e);

	default void barriersDump() {
	}
	
	default void await(SignalBarrier sb, int count) {
		try {
			sb.await(count);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
 