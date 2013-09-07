package ceki.keyword;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.joran.action.*;
import ch.qos.logback.classic.sift.SiftAction;
import ch.qos.logback.classic.spi.PlatformInfo;
import ch.qos.logback.core.joran.action.*;
import ch.qos.logback.core.joran.conditional.ElseAction;
import ch.qos.logback.core.joran.conditional.IfAction;
import ch.qos.logback.core.joran.conditional.ThenAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 6/20/13
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class LimitedJoranConfigurator extends JoranConfigurator {
  @Override
  public void addInstanceRules(RuleStore rs) {
    // is "configuration/variable" referenced in the docs?
    rs.addRule(new ElementSelector("configuration/variable"), new PropertyAction());
    rs.addRule(new ElementSelector("configuration/property"), new PropertyAction());

    rs.addRule(new ElementSelector("configuration/substitutionProperty"),
            new PropertyAction());

    rs.addRule(new ElementSelector("configuration/timestamp"), new TimestampAction());

    rs.addRule(new ElementSelector("configuration/define"), new DefinePropertyAction());

    // the contextProperty pattern is deprecated. It is undocumented
    // and will be dropped in future versions of logback
    rs.addRule(new ElementSelector("configuration/contextProperty"),
            new ContextPropertyAction());

    rs.addRule(new ElementSelector("configuration/conversionRule"),
            new ConversionRuleAction());

    rs.addRule(new ElementSelector("configuration/statusListener"),
            new StatusListenerAction());

    rs.addRule(new ElementSelector("configuration/appender"), new AppenderAction());
    rs.addRule(new ElementSelector("configuration/appender/appender-ref"),
            new AppenderRefAction());
    rs.addRule(new ElementSelector("configuration/newRule"), new NewRuleAction());

    //rs.addRule(new ElementSelector("*/param"), new ParamAction());

    rs.addRule(new ElementSelector("configuration"), new ConfigurationAction());

    rs.addRule(new ElementSelector("configuration/contextName"),
            new ContextNameAction());
    rs.addRule(new ElementSelector("configuration/contextListener"),
            new LoggerContextListenerAction());
    rs.addRule(new ElementSelector("configuration/insertFromJNDI"),
            new InsertFromJNDIAction());
    rs.addRule(new ElementSelector("configuration/evaluator"), new EvaluatorAction());

    rs.addRule(new ElementSelector("configuration/appender/sift"), new SiftAction());
    rs.addRule(new ElementSelector("configuration/appender/sift/*"), new NOPAction());

    rs.addRule(new ElementSelector("configuration/logger"), new LoggerAction());
    rs.addRule(new ElementSelector("configuration/logger/level"), new LevelAction());

    rs.addRule(new ElementSelector("configuration/root"), new RootLoggerAction());
    rs.addRule(new ElementSelector("configuration/root/level"), new LevelAction());
    rs.addRule(new ElementSelector("configuration/logger/appender-ref"),
            new AppenderRefAction());
    rs.addRule(new ElementSelector("configuration/root/appender-ref"),
            new AppenderRefAction());

    // add jmxConfigurator only if we have JMX available.
    // If running under JDK 1.4 (retrotranslateed logback) then we
    // might not have JMX.
    if (PlatformInfo.hasJMXObjectName()) {
      rs.addRule(new ElementSelector("configuration/jmxConfigurator"),
              new JMXConfiguratorAction());
    }
    rs.addRule(new ElementSelector("configuration/include"), new IncludeAction());

    rs.addRule(new ElementSelector("configuration/consolePlugin"),
            new ConsolePluginAction());

    rs.addRule(new ElementSelector("configuration/receiver"),
            new ReceiverAction());


  }
}
