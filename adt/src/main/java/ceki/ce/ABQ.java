package ceki.ce;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public E[] take() {
		
		try {
			E e0 = abq.take();
			List<E> container = new ArrayList<E>();
			container.add(e0);
			abq.drainTo(container);
			E[] values = (E[]) Array.newInstance(clazz, container.size());
			return container.toArray(values);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
