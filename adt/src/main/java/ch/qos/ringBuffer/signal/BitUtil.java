package ch.qos.ringBuffer.signal;

public class BitUtil {

	static public int leftMostBit(int n) {
		if (n == 0)
			return 0;

		int bitCount = 1;
		while (true) {
			n = n >>> 1;
			if (n == 0)
				break;
			else
				bitCount++;

		}
		return bitCount;
	}

}
