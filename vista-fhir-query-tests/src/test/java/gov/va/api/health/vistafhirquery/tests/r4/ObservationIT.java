package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class ObservationIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  private static Stream<Arguments> verifiers() {
    return Stream.of(
        arguments(VistaFhirQueryResourceVerifier.r4WithoutSite()),
        arguments(VistaFhirQueryResourceVerifier.r4ForSiteForTestPatient()));
  }

  @ParameterizedTest
  @MethodSource("verifiers")
  void readLaboratory(ResourceVerifier verifier) {
    assumeEnvironmentNotIn(Environment.STAGING);
    var path = "Observation/{observation}";
    verifier.verifyAll(
        verifier.test(200, Observation.class, path, testIds.observations().laboratory()));
  }

  @ParameterizedTest
  @MethodSource("verifiers")
  void readNotFound(ResourceVerifier verifier) {
    var path = "Observation/{observation}";
    verifier.verifyAll(verifier.test(404, OperationOutcome.class, path, "I3-404"));
  }

  @ParameterizedTest
  @MethodSource("verifiers")
  void readVitalSign(ResourceVerifier verifier) {
    assumeEnvironmentNotIn(Environment.STAGING, Environment.PROD);
    var path = "Observation/{observation}";
    verifier.verifyAll(
        verifier.test(200, Observation.class, path, testIds.observations().vitalSigns()));
  }

  @ParameterizedTest
  @MethodSource("verifiers")
  void search(ResourceVerifier verifier) {
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

  @ParameterizedTest
  @MethodSource("verifiers")
  void searchLaboratory(ResourceVerifier verifier) {
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

  @ParameterizedTest
  @MethodSource("verifiers")
  void searchNotMe(ResourceVerifier verifier) {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verify(
        verifier.test(
            403, OperationOutcome.class, "Observation?patient={patient}", testIds.unknown()));
  }

  @ParameterizedTest
  @MethodSource("verifiers")
  void searchVitalSign(ResourceVerifier verifier) {
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
