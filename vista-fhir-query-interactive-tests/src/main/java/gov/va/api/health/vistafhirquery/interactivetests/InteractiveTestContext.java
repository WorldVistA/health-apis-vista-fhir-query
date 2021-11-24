package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.r4.api.resources.Resource;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.util.Properties;

@Slf4j
public class InteractiveTestContext implements TestContext {
  private static final String PROPERTIES_FILE_BASE_PATH = "/sentinel/test_properties";
  String name;
  Properties properties;

  public InteractiveTestContext(String name) {
    loadProperties(name);
    this.name = name;
  }

  @Override
  public void create(Resource resource) {
    var url =
        InteractiveTestUrlBuilder.builder()
            .baseUrl(properties.getProperty("baseurl"))
            .resourceName(resource.getClass().getSimpleName())
            .site(properties.getProperty("site"))
            .build()
            .url();
    // TODO: get auth token
    var token = InteractiveTokenClient.builder().build().clientCredentialsToken();
    log.info("Token is PLACEHOLDER: {}", token);
    // TODO: Add auth token header to request
    RequestSpecification requestSpecification = RestAssured.given();
    var response =
        requestSpecification.header("Content-Type", "application/json").body(resource).post(url);
    log.info(response.body().prettyPrint());
    // TODO: Save response to disk
  }

  @SneakyThrows
  private void loadProperties(String name) {
    var properties = new Properties();
    try (FileInputStream testProperties =
        new FileInputStream(PROPERTIES_FILE_BASE_PATH + "/" + name + ".properties")) {
      properties.load(testProperties);
    }
    try (FileInputStream globalProperties =
        new FileInputStream(PROPERTIES_FILE_BASE_PATH + "/global.properties")) {
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
