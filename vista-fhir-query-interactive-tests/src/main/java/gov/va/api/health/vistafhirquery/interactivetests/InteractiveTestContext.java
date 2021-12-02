package gov.va.api.health.vistafhirquery.interactivetests;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.r4.api.resources.Resource;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileSystemUtils;

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
            .header("Content-Type", "application/json")
            .headers(Map.of("Authorization", "Bearer " + token))
            .body(resource)
            .post(url);
    saveResultsToDisk(url, response, Optional.of(resource));
  }

  @SneakyThrows
  private void saveResultsToDisk(URL url, Response response, Optional<Resource> body) {
    var resultsDir = new File(propertiesFileLocation, "results");
    var testResultsDir = new File(resultsDir, name);
    if (testResultsDir.exists()) {
      FileSystemUtils.deleteRecursively(testResultsDir);
    }
    testResultsDir.mkdirs();
    var responseFile = new File(testResultsDir, "response.json");
    writeFile(responseFile, response.prettyPrint());
    ObjectMapper mapper = JacksonConfig.createMapper();
    var responseMetaFile = new File(testResultsDir, "meta.json");
    writeFile(
        responseMetaFile,
        mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(
                TestMeta.builder()
                    .requestUrl(url.toString())
                    .time(Instant.now())
                    .httpStatus(response.getStatusCode())
                    .build()));
    if (body.isPresent()) {
      var requestFile = new File(testResultsDir, "request.json");
      writeFile(requestFile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
    }
  }

  @Override
  public <T extends IsResource> ResourceUrls urlsFor(Class<T> resourceType) {
    return StandardResourceUrls.builder()
        .baseUrl(property("baseurl"))
        .resourceName(resourceType.getSimpleName())
        .site(property("site"))
        .build();
  }

  @SneakyThrows
  private void writeFile(File file, String body) {
    file.createNewFile();
    try (var writer = new FileWriter(file, UTF_8)) {
      writer.write(body);
    }
  }
}
