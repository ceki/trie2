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

import java.util.concurrent.ArrayBlockingQueue;


/**
 * 
 * An implementation of {@link RingBuffer} interface based
 * on {@link ArrayBlockingQueue} to serve as a point of comparison.
 * 
 * @author ceki
 *
 * @param <E>
 */
public class ABQ<E> implements RingBuffer<E> {

	int capacity;

	final ArrayBlockingQueue<E> abq;
	Class<E> clazz;

	ABQ(int capacity, Class<E> clazz) {
		this.clazz = clazz;
		this.capacity = capacity;

		abq = new ArrayBlockingQueue<>(capacity);
	}

	@Override
	public E take() {
		try {
			E e = abq.take();
			return e;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return null;
		}

	}

	@Override
	public void put(E e) {
		try {
			abq.put(e);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

	}

}
