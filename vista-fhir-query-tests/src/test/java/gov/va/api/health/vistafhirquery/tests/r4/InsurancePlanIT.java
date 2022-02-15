package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class InsurancePlanIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Delegate
  private final ResourceVerifier verifier =
      VistaFhirQueryResourceVerifier.r4ForSiteForTestPatient();

  @Test
  void read() {
    // Requires LHS LIGHTHOUSE RPC GATEWAY to be deployed to vista
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    var path = "InsurancePlan/{id}";
    verifyAll(
        test(200, InsurancePlan.class, path, testIds.insurancePlan()),
        test(404, OperationOutcome.class, path, "I3-404"));
  }
}
