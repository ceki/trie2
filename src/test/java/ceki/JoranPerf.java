package ceki;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;

import java.io.File;

public class JoranPerf {


  LoggerContext context = new LoggerContext();


  @Test
  public void joranTask() throws JoranException {
    LimitedJoranConfigurator jc = new LimitedJoranConfigurator();
    //JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(new File(Constants.SAMPLE_XML));
  }

  @Test
  public void joranPerformanceTest() throws Exception {
    for (int i = 0; i < Constants.WARM_UP_COUNT; i++) {
      joranTask();
      context.reset();
    }

    long sum = 0;
    for (int i = 0; i < Constants.WARM_UP_COUNT; i++) {

      long start = System.nanoTime();
      joranTask();
      long end = System.nanoTime();
      long diff = (end - start) / (1000);
      sum += diff;
      context.reset();
    }
    long avg = sum/Constants.WARM_UP_COUNT;
    System.out.println("joranPerformanceTest in " + avg + " in micro-seconds");

  }
}
