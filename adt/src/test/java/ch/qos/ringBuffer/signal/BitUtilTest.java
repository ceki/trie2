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

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import ch.qos.ringBuffer.signal.BitUtil;

public class BitUtilTest {

	@Ignore
	@Test
	public void smoke() {
		int r = BitUtil.leftMostBit(1);
		assertEquals(1, r);
	}

	@Test
	public void min() {
		int r = BitUtil.leftMostBit(-1);
		assertEquals(32, r);
	}
	
	@Test
	public void negative() {
		int r = BitUtil.leftMostBit(-10);
		assertEquals(32, r);
	}
	
	@Ignore
	@Test
	public void more() {
		{
			int r = BitUtil.leftMostBit(3);
			assertEquals(2, r);
		}
		
		{
			int r = BitUtil.leftMostBit(65);
			assertEquals(7, r);
		}
		
		{
			int r = BitUtil.leftMostBit(Integer.MAX_VALUE);
			assertEquals(31, r);
		}
	}

}
