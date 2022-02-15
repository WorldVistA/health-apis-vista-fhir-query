package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.resources.Endpoint;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.SystemDefinitions;
import gov.va.api.health.vistafhirquery.tests.TestClients;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class EndpointIT {
  private final String apiPath = SystemDefinitions.systemDefinition().basePath().apiPath();

  static Predicate<Integer> empty() {
    return size -> size == 0;
  }

  static Predicate<Integer> notEmpty() {
    return empty().negate();
  }

  static Stream<Arguments> search() {
    final String patient = SystemDefinitions.systemDefinition().publicIds().patient();
    return Stream.of(
        arguments("r4/Endpoint", notEmpty()),
        arguments("r4/Endpoint?status=active", notEmpty()),
        arguments("r4/Endpoint?status=ANYTHING_NOT_ACTIVE", empty()));
  }

  static Stream<Arguments> searchByPatient() {
    final String patient = SystemDefinitions.systemDefinition().publicIds().patient();
    return Stream.of(
        arguments("r4/Endpoint?status=active&patient=" + patient, notEmpty()),
        arguments("r4/Endpoint?patient=" + patient, notEmpty()));
  }

  @Test
  void read() {
    var requestPath = apiPath + "r4/Endpoint/673";
    log.info("Verify {} is Endpoint (200)", requestPath);
    var endpoint = TestClients.basePath().get(requestPath).expect(200).expectValid(Endpoint.class);
    assertThat(endpoint.id()).isEqualTo("673");
  }

  @ParameterizedTest
  @MethodSource
  void search(String query, Predicate<Integer> bundleSizeCondition) {
    var requestPath = apiPath + query;
    log.info("Verify {} is Bundle (200)", requestPath);
    var bundle =
        TestClients.basePath().get(requestPath).expect(200).expectValid(Endpoint.Bundle.class);
    assertThat(bundle.total()).matches(bundleSizeCondition);
  }

  @ParameterizedTest
  @MethodSource
  void searchByPatient(String query, Predicate<Integer> bundleSizeCondition) {
    assumeEnvironmentNotIn(Environment.STAGING);
    search(query, bundleSizeCondition);
  }
}
