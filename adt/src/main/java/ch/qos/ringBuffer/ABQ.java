package ch.qos.ringBuffer;

import java.util.concurrent.ArrayBlockingQueue;

public class ABQ<E> implements ICylicBuffer<E> {

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
		//List<E> container = new ArrayList<E>();

		// abq.drainTo(container);
		//E[] values = (E[]) Array.newInstance(clazz, 1);

		try {
			E e = abq.take();
			return e;
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		
//		E[] values = (E[]) Array.newInstance(clazz, container.size());
//		for(int i = 0; i < container.size(); i++) {
//			values[i] = container.get(i);
//		}
		//return values;
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
