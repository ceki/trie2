package ceki.keyword;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.event.stax.StaxEvent;
import ch.qos.logback.core.joran.event.stax.StaxEventRecorder;
import ch.qos.logback.core.joran.spi.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class TriePerf {
  Trie<String> trie = new Trie<String>();
  static Context context = new ContextBase();

  //static StaxEventRecorder staxEventRecorder = new StaxEventRecorder(context);
  //static SaxEventRecorder saxEventRecorder = new SaxEventRecorder(context);

  @BeforeClass
  static public void beforeClass() throws Exception {
    //saxReadIn(new File(TestConstants.SAMPLE_XML), saxEventRecorder);
    //staxReadIn(new File(TestConstants.SAMPLE_XML), staxEventRecorder);
  }

  void put(String k) {
    trie.put(k, k);
  }

  void buildTrie() {
    put("configuration");
    put("configuration/contextName");
    put("configuration/contextListener");
    put("configuration/insertFromJNDI");
    put("configuration/evaluator");
    put("configuration/appender/sift");
    put("configuration/logger");
    put("configuration/logger/level");
    put("configuration/root");
    put("configuration/root/level");
    put("configuration/logger/appender-ref");
    put("configuration/root/appender-ref");
    put("configuration/jmxConfigurator");
    put("configuration/include");
    put("configuration/consolePlugin");
    put("configuration/receiver");
    put("configuration/variable");
    put("configuration/property");
    put("configuration/substitutionProperty");
    put("configuration/timestamp");
    put("configuration/define");
    put("configuration/contextProperty");
    put("configuration/conversionRule");
    put("configuration/statusListener");
    put("configuration/appender");
    put("configuration/appender/appender-ref");
    put("configuration/newRule");
  }


  void buildRuleStore(RuleStore rs) {
    rs.addRule(new ElementSelector("configuration"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/contextName"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/contextListener"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/insertFromJNDI"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/evaluator"), new NOPAction());

    rs.addRule(new ElementSelector("configuration/appender/sift"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/logger"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/logger/level"), new NOPAction());

    rs.addRule(new ElementSelector("configuration/root"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/root/level"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/logger/appender-ref"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/root/appender-ref"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/jmxConfigurator"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/include"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/consolePlugin"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/receiver"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/variable"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/property"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/substitutionProperty"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/timestamp"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/define"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/contextProperty"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/conversionRule"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/statusListener"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/appender"), new NOPAction());
    rs.addRule(new ElementSelector("configuration/appender/appender-ref"),
            new NOPAction());
    rs.addRule(new ElementSelector("configuration/newRule"), new NOPAction());
  }

  @Test
  public void trieBasedPerformance_SAX() throws Exception {
    for (int i = 0; i < TestConstants.WARM_UP_COUNT; i++) {
      trieBasedTask_SAX();
//      if(i==0)
//        System.out.println(trie.root);
      trie.clear();
    }

    long sum = 0;
    for (int i = 0; i < TestConstants.WARM_UP_COUNT; i++) {
      long start = System.nanoTime();
      trieBasedTask_SAX();
      trie.clear();
      long end = System.nanoTime();
      long diff = (end - start) / (1000);
      sum += diff;
    }

    long avg = sum / TestConstants.WARM_UP_COUNT;
    System.out.println("trieBasedTask_SAX performance in " + avg + " in micro-seconds");
  }

  @Test
  @Ignore
  public void trieBasedPerformance_STAX() throws Exception {
    for (int i = 0; i < TestConstants.WARM_UP_COUNT; i++) {
      trieBasedTask_STAX();
//      if(i==0)
//        System.out.println(trie.root);
      trie.clear();
    }

    long sum = 0;
    for (int i = 0; i < TestConstants.WARM_UP_COUNT; i++) {
      long start = System.nanoTime();
      trieBasedTask_STAX();
      trie.clear();
      long end = System.nanoTime();
      long diff = (end - start) / (1000);
      sum += diff;
    }

    long avg = sum / TestConstants.WARM_UP_COUNT;
    System.out.println("trieBasedTask_STAX performance in " + avg + " in micro-seconds");
  }


  @Test
  public void ruleStoreBasedPerformance() throws Exception {
    for (int i = 0; i < TestConstants.WARM_UP_COUNT; i++) {
      ruleStoreBasedTask();
    }

    long sum = 0;
    for (int i = 0; i < TestConstants.WARM_UP_COUNT; i++) {
      long start = System.nanoTime();
      ruleStoreBasedTask();
      long end = System.nanoTime();
      long diff = (end - start) / (1000);
      sum += diff;
    }
    long avg = sum / TestConstants.WARM_UP_COUNT;
    System.out.println("ruleStoreBasedPerformance in " + avg + " in micro-seconds");
  }

  static SaxEventRecorder saxReadIn(File file, SaxEventRecorder recorder) throws IOException, JoranException {
    FileInputStream inputStream = new FileInputStream(file);
    InputSource inputSource = new InputSource(inputStream);
    recorder.recordEvents(inputSource);
    inputStream.close();
    return recorder;
  }

  static void staxReadIn(File file, StaxEventRecorder recorder) throws IOException, JoranException {
    FileInputStream inputStream = new FileInputStream(file);
    recorder.recordEvents(inputStream);
    inputStream.close();
  }


  private void ruleStoreBasedTask() throws Exception {
    SimpleRuleStore rs = new SimpleRuleStore(context);
    buildRuleStore(rs);
    SaxEventRecorder saxEventRecorder = new SaxEventRecorder(context);
    saxReadIn(new File(TestConstants.SAMPLE_XML), saxEventRecorder);

    for (SaxEvent saxEvent : saxEventRecorder.getSaxEventList()) {
      if (saxEvent instanceof StartEvent) {
        StartEvent startEvent = (StartEvent) saxEvent;
        List<Action> result = rs.matchActions(startEvent.elementPath);
      }
    }
  }

  private void trieBasedTask_STAX() throws Exception {
    buildTrie();
    StaxEventRecorder staxEventRecorder = new StaxEventRecorder(context);
    staxReadIn(new File(TestConstants.SAMPLE_XML), staxEventRecorder);

    for (StaxEvent staxEvent : staxEventRecorder.getEventList()) {
      if (staxEvent instanceof ch.qos.logback.core.joran.event.stax.StartEvent) {
        ch.qos.logback.core.joran.event.stax.StartEvent startEvent = (ch.qos.logback.core.joran.event.stax.StartEvent) staxEvent;
        String eventAsKeyword = toKeyword(startEvent.elementPath);
        String result = trie.get(eventAsKeyword);
      }
    }
  }


  private void trieBasedTask_SAX() throws Exception {
    buildTrie();
    SaxEventRecorder saxEventRecorder = new SaxEventRecorder(context);
    saxReadIn(new File(TestConstants.SAMPLE_XML), saxEventRecorder);

    for (SaxEvent saxEvent : saxEventRecorder.getSaxEventList()) {
      if (saxEvent instanceof StartEvent) {
        StartEvent startEvent = (StartEvent) saxEvent;
        String eventAsKeyword = toKeyword(startEvent.elementPath);
        String result = trie.get(eventAsKeyword);
      }
    }
  }

  String toKeyword(ElementPath elementPath) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < elementPath.size(); i++) {
      if (i != 0)
        sb.append('/');
      sb.append(elementPath.get(i));
    }
    return sb.toString();
  }

}
