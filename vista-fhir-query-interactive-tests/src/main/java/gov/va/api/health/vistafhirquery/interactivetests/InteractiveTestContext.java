package gov.va.api.health.vistafhirquery.interactivetests;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    log.info("Requesting {}", url);
    var response = requestSpecification().body(resource).post(url);
    saveResultsToDisk(url, response, Optional.of(resource));
  }

  private void logMetadata(TestMeta metadata) {
    log.info("URL ...... {}", metadata.requestUrl());
    log.info("Status ... {}", metadata.httpStatus());
    if (isNotBlank(metadata.created())) {
      log.info("Created .. {}", metadata.created());
    }
  }

  @Override
  public void read(Resource resource, String id) {
    var url = urlsFor(resource).read(id);
    log.info("Requesting {}", url);
    var response = requestSpecification().get(url);
    saveResultsToDisk(url, response, Optional.empty());
  }

  private RequestSpecification requestSpecification() {
    var token = InteractiveTokenClient.builder().ctx(this).build().clientCredentialsToken();
    log.info("Authorization:\nexport T=\"{}\"", token);
    return RestAssured.given()
        .headers("Content-Type", "application/json")
        .headers(Map.of("Authorization", "Bearer " + token));
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
    TestMeta metadata =
        TestMeta.builder()
            .requestUrl(url.toString())
            .time(Instant.now())
            .httpStatus(response.getStatusCode())
            .created(response.getHeader("Location"))
            .responseHeaders(response.getHeaders().asList())
            .build();
    logMetadata(metadata);
    writeFile(
        responseMetaFile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata));
    if (body.isPresent()) {
      var requestFile = new File(testResultsDir, "request.json");
      writeFile(requestFile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
    }
  }

  @Override
  @SneakyThrows
  public void search(Resource resource, Map<String, String> map) {
    var url = urlsFor(resource).search(map);
    log.info("Requesting {}", url);
    var response = requestSpecification().get(url);
    saveResultsToDisk(url, response, Optional.empty());
  }

  @Override
  @SneakyThrows
  public void update(Resource resource) {
    var url = urlsFor(resource).update(resource.id());
    log.info("Requesting {}", url);
    var response = requestSpecification().body(resource).put(url);
    saveResultsToDisk(url, response, Optional.of(resource));
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
