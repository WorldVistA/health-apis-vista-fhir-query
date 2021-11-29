package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.r4.api.resources.Resource;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InteractiveTestContext implements TestContext, TestProperties {
  private final String name;

  @Delegate private final TestProperties properties;

  private final File propertiesFileLocation;

  /** Constructor that loads properties files for the test execution. */
  public InteractiveTestContext(String name) {
    this.name = name;
    this.propertiesFileLocation =
        new File(
            System.getProperty(
                "interactive-tests.test-properties", System.getProperty("user.dir")));
    this.properties =
        HierarchicalTestProperties.builder()
            .globalPropertiesFile(new File(propertiesFileLocation, "global.properties"))
            .testPropertiesFile(new File(propertiesFileLocation, name + ".properties"))
            .build();
  }

  @Override
  @SneakyThrows
  public void create(Resource resource) {
    var url = urlsFor(resource).create();
    var token = InteractiveTokenClient.builder().ctx(this).build().clientCredentialsToken();
    log.info("Requesting {}", url);
    log.info("Authorization:\nexport T=\"{}\"", token);
    RequestSpecification requestSpecification = RestAssured.given();
    var response =
        requestSpecification
            .log()
            .everything()
            .header("Content-Type", "application/json")
            .headers(Map.of("Authorization", "Bearer " + token))
            .body(resource)
            .post(url);
    saveResultsToDisk(response);
  }

  @SneakyThrows
  private void saveResultsToDisk(Response response) {
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

  @Override
  public <T extends IsResource> ResourceUrls urlsFor(Class<T> resourceType) {
    return StandardResourceUrls.builder()
        .baseUrl(property("baseurl"))
        .resourceName(resourceType.getSimpleName())
        .site(property("site"))
        .build();
  }
}
