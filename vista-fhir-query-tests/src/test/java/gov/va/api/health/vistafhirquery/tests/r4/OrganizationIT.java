package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
public class OrganizationIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Delegate
  private final ResourceVerifier verifier = VistaFhirQueryResourceVerifier.r4ForSite("673");

  @Test
  void read() {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    var path = "Organization/{id}";
    verifyAll(
        test(200, Organization.class, path, testIds.organizations().insTypeRead()),
        test(200, Organization.class, path, testIds.organizations().payTypeRead()),
        test(404, OperationOutcome.class, path, "I3-404"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"type=ins", "type=pay"})
  void search(String query) {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    var path = "Organization?" + query;
    verifyAll(test(200, Organization.Bundle.class, path));
  }
}
