package gov.va.api.health.vistafhirquery.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.ServiceDefinition;
import gov.va.api.health.vistafhirquery.tests.TestIds.IcnAtSites;
import gov.va.api.lighthouse.testclients.OauthClient;
import gov.va.api.lighthouse.testclients.PropertiesLoader;
import gov.va.api.lighthouse.testclients.RetryingOauthClient;
import gov.va.api.lighthouse.testclients.selenium.WebDriverConfiguration;
import gov.va.api.lighthouse.testclients.ssoi.SsoiOauthClient;
import gov.va.api.lighthouse.testclients.ssoi.SsoiRequestConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class VistaConnectivityIT {
  private static String token;

  @BeforeAll
  static void acquireToken() {
    if (Environment.LOCAL == Environment.get()) {
      token = "not-used";
      return;
    }
    var client = tryToBuildSsoiClient();
    assumeThat(client).as(client.getClass().getSimpleName() + " cannot be created.").isPresent();
    var tokenResponse = client.get().requestToken();
    assertThat(tokenResponse.isError())
        .as(
            "Failed to get access token: "
                + tokenResponse.error()
                + " -- "
                + tokenResponse.errorDescription())
        .isFalse();
    token = tokenResponse.accessToken();
    log.info("Acquired authorization token.");
  }

  @SuppressWarnings("unused")
  static Stream<Arguments> connected() {
    return SystemDefinitions.systemDefinition().publicIds().patientSites().stream()
        .map(Arguments::of);
  }

  private static Optional<OauthClient> tryToBuildSsoiClient() {
    var propertiesLoader = PropertiesLoader.usingSystemProperties();
    try {
      var webDriverConfig = WebDriverConfiguration.fromProperties(propertiesLoader);
      var ssoiConfig = SsoiRequestConfiguration.fromProperties(propertiesLoader);
      return Optional.of(RetryingOauthClient.of(SsoiOauthClient.of(ssoiConfig, webDriverConfig)));
    } catch (IllegalArgumentException e) {
      log.error("Failed to create oauth client: " + e.getMessage());
      return Optional.empty();
    }
  }

  @ParameterizedTest
  @MethodSource
  void connected(IcnAtSites icnAtSites) {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    var sd = SystemDefinitions.systemDefinition().basePath();
    log.info("Testing connectivity with {}:{} with path {}", sd.url(), sd.port(), sd.apiPath());
    ExpectedResponse bundleResponse =
        ExpectedResponse.of(
            request(sd)
                .request(
                    Method.GET,
                    sd.apiPath() + "hcs/{site}/r4/Observation?patient={icn}",
                    icnAtSites.vistas().get(0),
                    icnAtSites.icn()));
    var bundle = bundleResponse.expect(200).expectValid(Observation.Bundle.class);
    assertThat(bundle.total()).isGreaterThan(0);
  }

  private RequestSpecification request(ServiceDefinition sd) {
    return RestAssured.given()
        .baseUri(sd.url())
        .port(sd.port())
        .relaxedHTTPSValidation()
        .headers(Map.of("Authorization", "Bearer " + token))
        .contentType("application/json")
        .accept("application/json")
        .log()
        .uri();
  }
}
