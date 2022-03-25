package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class MedicationDispenseIT {

  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Delegate
  private final ResourceVerifier verifier =
      VistaFhirQueryResourceVerifier.r4ForSiteForTestPatient();

  @Test
  void medDispenseRead() {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    verifyAll(
        test(
            200,
            MedicationDispense.class,
            "MedicationDispense/{medicationdispense}",
            testIds.medicationDispense()),
        test(404, OperationOutcome.class, "MedicationDispense/{medicationdispense}", "I3-404"));
  }

  @Test
  void medDispenseSearch() {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    verifyAll(
        test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?patient={icn}",
            testIds.patient()),
        test(
            200,
            MedicationDispense.Bundle.class,
            "MedicationDispense?whenprepared=gt2008&patient={icn}",
            testIds.patient()),
        test(
            400,
            OperationOutcome.class,
            "MedicationDispense?whenprepared=lt2010&whenprepared=ge2012&patient={icn}",
            testIds.patient()));
  }
}
