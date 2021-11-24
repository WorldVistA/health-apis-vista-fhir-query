package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.r4.api.resources.Resource;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InteractiveTestContext implements TestContext {
  String name;
  Properties properties;
  String propertiesFileLocation;

  /** Constructor that loads properties files for the test execution. */
  public InteractiveTestContext(String name) {
    this.propertiesFileLocation =
        System.getProperty("interactive-tests.test-properties", System.getProperty("user.dir"));
    loadProperties(name);
    this.name = name;
  }

  @Override
  @SneakyThrows
  public void create(Resource resource) {
    var url =
        InteractiveTestUrlBuilder.builder()
            .baseUrl(properties.getProperty("baseurl"))
            .resourceName(resource.getClass().getSimpleName())
            .site(properties.getProperty("site"))
            .build()
            .createUrl();
    var token = InteractiveTokenClient.builder().ctx(this).build().clientCredentialsToken();
    log.info("Client Credentials Token is: {}", token);
    RequestSpecification requestSpecification = RestAssured.given();
    var response =
        requestSpecification
            .header("Content-Type", "application/json")
            .headers(Map.of("Authorization", "Bearer " + token))
            .body(resource)
            .post(url);
    new File(propertiesFileLocation + "/responses").mkdir();
    new File(propertiesFileLocation + "/responses/" + name + ".response").createNewFile();
    FileWriter fw =
        new FileWriter(
            propertiesFileLocation + "/responses/" + name + ".response_" + Instant.now(),
            StandardCharsets.UTF_8);
    fw.write(String.format("%s%n", response.statusCode()));
    fw.write(response.prettyPrint());
    fw.close();
  }

  @SneakyThrows
  private void loadProperties(String name) {
    var properties = new Properties();
    try (FileInputStream testProperties =
        new FileInputStream(propertiesFileLocation + "/" + name + ".properties")) {
      properties.load(testProperties);
    }
    try (FileInputStream globalProperties =
        new FileInputStream(propertiesFileLocation + "/global.properties")) {
      properties.load(globalProperties);
    }
    this.properties = properties;
    log.info("Properties are: {}", properties.stringPropertyNames());
  }

  @Override
  public String property(String key) {
    var value = properties.getProperty(key);
    if (value == null) {
      throw new IllegalArgumentException(
          String.format(
              "Missing configuration property: %s. Check the %s.properties file.", key, name));
    }
    return value;
  }
}
