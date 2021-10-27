package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class AppointmentIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Delegate
  private final ResourceVerifier verifier = VistaFhirQueryResourceVerifier.r4ForSite("673");

  @Test
  void search() {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    verifyAll(
        test(200, Appointment.Bundle.class, "Appointment?patient={icn}", testIds.patient()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?date=ge2010&date=lt2012&patient={icn}",
            testIds.patient()),
        test(
            400,
            OperationOutcome.class,
            "Appointment?date=lt2010&date=ge2012&patient={icn}",
            testIds.patient()));
  }
}
