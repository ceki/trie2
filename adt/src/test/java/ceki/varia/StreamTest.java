package ceki.varia;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.Ignore;

public class StreamTest {

	@Test
        @Ignore
	public void x() {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");

		Stream<String> stream = list.stream();

		System.out.println(stream.count());
		stream.forEach((e) -> {
			System.out.println(e);
		});

	}
}
