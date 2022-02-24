package gov.va.api.health.vistafhirquery.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.Environment;
import io.restassured.http.Method;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class RawIT {

  private static List<String> goodRequest() {
    TestIds testIds = VistaFhirQueryResourceVerifier.ids();
    var icnAtSite = testIds.patientSites().get(0);
    return List.of(
        "/internal/raw/CoverageEligibilityResponse?icn="
            + icnAtSite.icn()
            + "&hcs="
            + icnAtSite.vistas().get(0),
        "/internal/raw/Organization?id=" + testIds.organizations().insTypeRead(),
        "/internal/raw/Organization?id=" + testIds.organizations().payTypeRead());
  }

  @ParameterizedTest
  @MethodSource("goodRequest")
  void clientKeyIsMissing(String path) {
    assumeEnvironmentIn(Environment.LOCAL);
    var response =
        SystemDefinitions.systemDefinition()
            .internal()
            .requestSpecification()
            .log()
            .uri()
            .headers(Map.of("client-key", "nope"))
            .contentType("application/json")
            .accept("application/json")
            .request(Method.GET, path);
    assertThat(response.getStatusCode()).isEqualTo(401);
  }

  @ParameterizedTest
  @MethodSource
  void goodRequest(String requestUrl) {
    assumeEnvironmentIn(Environment.LOCAL);
    log.info("Verify raw response for {} is [200]", requestUrl);
    var response =
        SystemDefinitions.systemDefinition()
            .internal()
            .requestSpecification()
            .log()
            .uri()
            .headers(Map.of("client-key", "~shanktopus~"))
            .contentType("application/json")
            .accept("application/json")
            .request(Method.GET, requestUrl);
    assertThat(response.getStatusCode()).isEqualTo(200);
  }
}
