package ch.qos.ringBuffer.signal;

public class SignalBarrierFactory {

	
	
	// SignalBarier consumerSignalBarrier = new MixedSignalBarrierWithBackOff(MAX_YEILD_COUNT, PARK_DURATION);
    // SignalBarier producerSignalBarrier = new ParkNanosSignalBarrier(PARK_DURATION);

	
	static public SignalBarrier makeSignalBarrier() {
		return new BusyWaitSignalBarrier();
	}
}
