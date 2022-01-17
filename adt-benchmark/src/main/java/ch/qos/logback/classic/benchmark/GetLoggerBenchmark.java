
package ch.qos.logback.classic.benchmark;


import org.apache.logging.log4j.LogManager;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
//import org.pure4j.collections.PersistentTreeMap;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

@State(Scope.Benchmark)
public class GetLoggerBenchmark {

	LoggerContext loggerContext;

	 @Setup
	 public void setup() {
		 
		 loggerContext = new LoggerContext();
		 loggerContext.start();
		 
		
			
	 }

	 
	@Benchmark
	public void getLog4j2Logger() {
		LogManager.getLogger(this.getClass());
	}

	@Benchmark
	public void getLogbackLogger() {
		LoggerFactory.getLogger(this.getClass());
	}

}
