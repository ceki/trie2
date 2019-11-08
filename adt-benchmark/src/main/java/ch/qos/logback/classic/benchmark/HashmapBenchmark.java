package ch.qos.logback.classic.benchmark;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.pure4j.collections.PersistentTreeMap;

import com.romix.scala.collection.concurrent.TrieMap;

@Threads(64)
@State(Scope.Benchmark)
public class HashmapBenchmark {

	ConcurrentHashMap<String, Integer> concurrentHashMap;
    ConcurrentMap<String, Integer> trieMap;
    PersistentTreeMap<String, Integer> persistentTreeMap;
    
    String key = this.getClass().getName();
	
	   
	@Setup
	public void setup() {
		concurrentHashMap = new ConcurrentHashMap<>();
		concurrentHashMap.put(key, 1);
		
		trieMap = new TrieMap<>();
		trieMap.put(key, 1);
		
	   persistentTreeMap = PersistentTreeMap.emptyMap();
	   persistentTreeMap = persistentTreeMap.assoc(key, 1);
	}

	@Benchmark
	public void concurrentHashMapGet() {
		concurrentHashMap.get(key);	
	}

	@Benchmark
	public void trieMapGet() {
		trieMap.get(key);
	}

	@Benchmark
	public void persistentTreeMapGet() {
		persistentTreeMap.get(key);
	}
	
}
