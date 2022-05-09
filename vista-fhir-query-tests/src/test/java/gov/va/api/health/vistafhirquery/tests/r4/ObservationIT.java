package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ObservationIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  private final ResourceVerifier verifier =
      VistaFhirQueryResourceVerifier.r4ForSiteForTestPatient();

  @Test
  void readLaboratory() {
    assumeEnvironmentNotIn(Environment.STAGING);
    var path = "Observation/{observation}";
    verifier.verifyAll(
        verifier.test(200, Observation.class, path, testIds.observations().laboratory()));
  }

  @Test
  void readNotFound() {
    var path = "Observation/{observation}";
    verifier.verifyAll(verifier.test(404, OperationOutcome.class, path, "I3-404"));
  }

  @Test
  void readVitalSign() {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    var path = "Observation/{observation}";
    verifier.verifyAll(
        verifier.test(200, Observation.class, path, testIds.observations().vitalSigns()));
  }

  @Test
  void search() {
    assumeEnvironmentNotIn(Environment.STAGING);
    verifier.verifyAll(
        verifier.test(
            200,
            Observation.Bundle.class,
            R4TestSupport::isBundleWithAtLeastOneEntry,
            "Observation?patient={patient}",
            testIds.patient()),
        verifier.test(
            200,
            Observation.Bundle.class,
            R4TestSupport::isBundleWithAtLeastOneEntry,
            "Observation?patient={patient}&code={code}",
            testIds.patient(),
            testIds.observations().code()),
        verifier.test(
            200,
            Observation.Bundle.class,
            R4TestSupport::isBundleWithAtLeastOneEntry,
            "Observation?patient={patient}&date=ge2010&date=lt2012",
            testIds.patient()),
        verifier.test(
            400,
            OperationOutcome.class,
            "Observation?patient={patient}&date=ge2012&date=lt2010",
            testIds.patient()));
  }

  @Test
  void searchLaboratory() {
    assumeEnvironmentNotIn(Environment.STAGING);
    verifier.verifyAll(
        verifier.test(
            200,
            Observation.Bundle.class,
            R4TestSupport::isBundleWithAtLeastOneEntry,
            "Observation?_id={id}",
            testIds.observations().laboratory()),
        verifier.test(
            200,
            Observation.Bundle.class,
            R4TestSupport::isBundleWithAtLeastOneEntry,
            "Observation?identifier={id}",
            testIds.observations().laboratory()),
        verifier.test(
            200,
            Observation.Bundle.class,
            R4TestSupport::isBundleWithAtLeastOneEntry,
            "Observation?patient={patient}&category=laboratory",
            testIds.patient()));
  }

  @Test
  void searchVitalSign() {
    assumeEnvironmentNotIn(Environment.STAGING);
    verifier.verifyAll(
        verifier.test(
            200,
            Observation.Bundle.class,
            (Environment.get() == Environment.PROD)
                ? R4TestSupport::isAnyBundle
                : R4TestSupport::isBundleWithAtLeastOneEntry,
            "Observation?patient={patient}&category=vital-signs",
            testIds.patient()));
  }
}
