package ceki.ce.signal;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

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
