package gov.va.api.health.vistafhirquery.tests.r4;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Endpoint;
import gov.va.api.health.vistafhirquery.tests.SystemDefinitions;
import gov.va.api.health.vistafhirquery.tests.TestClients;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
public class EndpointIT {

  private final String apiPath = SystemDefinitions.systemDefinition().basePath().apiPath();

  @ParameterizedTest
  @ValueSource(strings = {"r4/Endpoint", "r4/Endpoint?status=active"})
  void search(String query) {
    var requestPath = apiPath + query;
    log.info("Verify {} is Bundle (200)", requestPath);
    var bundle =
        TestClients.basePath().get(requestPath).expect(200).expectValid(Endpoint.Bundle.class);
    assertThat(bundle.total()).isGreaterThan(0);
  }

  @Test
  void searchWithBadStatus() {
    var requestPath = apiPath + "r4/Endpoint?status=INVALID";
    log.info("Verify {} is Bundle (200)", requestPath);
    var bundle =
        TestClients.basePath().get(requestPath).expect(200).expectValid(Endpoint.Bundle.class);
    assertThat(bundle.total()).isEqualTo(0);
  }
}
