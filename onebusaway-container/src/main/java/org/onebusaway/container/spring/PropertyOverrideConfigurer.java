package org.onebusaway.container.spring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Extension of Spring's
 * {@link org.springframework.beans.factory.config.PropertyOverrideConfigurer}
 * that supports {@link System#getProperty(String)} expansion of
 * 
 * <pre class="code">${...}</pre>
 * 
 * property expressions in override values.
 * 
 * @author bdferris
 * 
 */
public class PropertyOverrideConfigurer extends
    org.springframework.beans.factory.config.PropertyOverrideConfigurer {

  private static Logger _log = LoggerFactory.getLogger(PropertyOverrideConfigurer.class);

  private static final Pattern _pattern = Pattern.compile("\\$\\{([^}]+)\\}");

  @Override
  protected void applyPropertyValue(ConfigurableListableBeanFactory factory,
      String beanName, String property, String value) {
    if (value != null)
      value = resolveValue(value);
    super.applyPropertyValue(factory, beanName, property, value);
  }

  protected String resolveValue(String value) {
    Matcher m = _pattern.matcher(value);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String property = m.group(1);
      String propertyValue = System.getProperty(property);
      if (propertyValue == null) {
        _log.warn("no such System property: " + property);
      } else {
        propertyValue = resolveValue(propertyValue);
      }
      m.appendReplacement(sb, propertyValue);
    }
    m.appendTail(sb);
    return sb.toString();
  }

}
