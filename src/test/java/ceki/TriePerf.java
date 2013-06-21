package ceki;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.event.StartEvent;
import ch.qos.logback.core.joran.spi.*;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class TriePerf {


  Trie<String> trie = new Trie<String>();
  Context context = new ContextBase();
  SaxEventRecorder recorder;

  @Before
  public void setUp() throws Exception {
    recorder = readIn(new File(TestConstants.SAMPLE_XML));
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
  public void trieBasedPerformance() throws Exception {
    for (int i = 0; i < TestConstants.WARM_UP_COUNT; i++) {
      trieBasedTask();
      if(i==0)
        System.out.println(trie.root);
      trie.clear();
    }

    long sum = 0;
    for (int i = 0; i < TestConstants.WARM_UP_COUNT; i++) {
      long start = System.nanoTime();
      trieBasedTask();
      trie.clear();
      long end = System.nanoTime();
      long diff = (end - start) / (1000);
      sum += diff;
    }

    long avg = sum / TestConstants.WARM_UP_COUNT;
    System.out.println("trieBasedPerformance in " + avg + " in micro-seconds");
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

  SaxEventRecorder readIn(File file) throws IOException, JoranException {
    FileInputStream inputStream = new FileInputStream(file);
    InputSource inputSource = new InputSource(inputStream);
    SaxEventRecorder recorder = new SaxEventRecorder(context);
    recorder.recordEvents(inputSource);
    inputStream.close();
    return recorder;
  }

  private void ruleStoreBasedTask() throws Exception {
    SimpleRuleStore rs = new SimpleRuleStore(context);
    buildRuleStore(rs);
    SaxEventRecorder recorder = readIn(new File(TestConstants.SAMPLE_XML));

    for (SaxEvent saxEvent : recorder.getSaxEventList()) {
      if (saxEvent instanceof StartEvent) {
        StartEvent startEvent = (StartEvent) saxEvent;
        List<Action> result = rs.matchActions(startEvent.elementPath);
        //System.out.println(startEvent.elementPath + " -> " + result);

      }
    }
  }

  private void trieBasedTask() throws Exception {
    buildTrie();
    //SaxEventRecorder recorder = readIn(new File(TestConstants.SAMPLE_XML));
    for (SaxEvent saxEvent : recorder.getSaxEventList()) {
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
