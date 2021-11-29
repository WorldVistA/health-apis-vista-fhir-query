package gov.va.api.health.vistafhirquery.interactivetests;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.SneakyThrows;

public class HierarchicalTestProperties implements TestProperties {
  private final File globalPropertiesFile;
  private final File testPropertiesFile;
  private final Properties properties;

  /** Load the global, test-specific, and system properties. */
  @Builder
  public HierarchicalTestProperties(File globalPropertiesFile, File testPropertiesFile) {
    this.globalPropertiesFile = globalPropertiesFile;
    this.testPropertiesFile = testPropertiesFile;
    this.properties = loadProperties();
  }

  @SneakyThrows
  private Properties loadProperties() {
    var globalProperties = new Properties();
    try (var is = new FileInputStream(globalPropertiesFile)) {
      globalProperties.load(is);
    }

    var testProperties = new Properties(globalProperties);
    try (var is = new FileInputStream(testPropertiesFile)) {
      testProperties.load(is);
    }

    var effectiveProperties = new Properties(testProperties);
    Stream.concat(
            globalProperties.stringPropertyNames().stream(),
            testProperties.stringPropertyNames().stream())
        .forEach(
            name -> {
              var override = System.getProperty(name);
              if (override != null) {
                effectiveProperties.setProperty(name, override);
              }
            });
    return effectiveProperties;
  }

  @Override
  public String property(String name) {
    var value = properties.getProperty(name);
    if (value == null) {
      throw new PropertyNotDefined(name);
    }
    return value;
  }
}
