package ch.qos.ringBuffer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JCtoolsTBTest {
	JCToolsRB<Integer> jctoolsRB = new JCToolsRB<>(16);
	
	@Test
	public void smokeABQ() {
		jctoolsRB.put(1);
		Integer val = jctoolsRB.take();
		Integer expected = 1;
		assertEquals(expected, val);
	}
}
