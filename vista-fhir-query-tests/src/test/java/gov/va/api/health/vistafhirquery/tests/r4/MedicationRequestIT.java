package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class MedicationRequestIT {

  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Delegate
  private final ResourceVerifier verifier = VistaFhirQueryResourceVerifier.r4ForSiteForTestPatient();

  @Test
  void medRequestRead() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifyAll(
        test(
            200,
            MedicationRequest.class,
            "MedicationRequest/{medicationrequest}",
            testIds.medicationRequest()),
        test(404, OperationOutcome.class, "MedicationRequest/{medicationrequest}", "I3-404"));
  }

  @Test
  void medRequestSearch() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifyAll(
        test(
            200,
            MedicationRequest.Bundle.class,
            "MedicationRequest?patient={icn}",
            testIds.patient()));
  }
}
