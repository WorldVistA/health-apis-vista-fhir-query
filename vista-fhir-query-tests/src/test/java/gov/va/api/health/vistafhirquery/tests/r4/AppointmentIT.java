package gov.va.api.health.vistafhirquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.fhir.testsupport.ResourceVerifier;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.vistafhirquery.tests.TestIds;
import gov.va.api.health.vistafhirquery.tests.VistaFhirQueryResourceVerifier;
import java.time.Year;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class AppointmentIT {
  private final TestIds testIds = VistaFhirQueryResourceVerifier.ids();

  @Delegate
  private final ResourceVerifier verifier =
      VistaFhirQueryResourceVerifier.r4ForSiteForTestPatient();

  @Test
  void read() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifyAll(test(200, Appointment.class, "Appointment/{appointment}", testIds.appointment()));
  }

  @Test
  void readNotFound() {
    verifyAll(test(404, OperationOutcome.class, "Appointment/{appointment}", "I3-404"));
  }

  @Test
  void search() {
    assumeEnvironmentNotIn(Environment.STAGING);
    /*
     * Appointments in the synthetic environment are created every day and we do not know in advance
     * what the IDs will be. Let's search and use one we find.
     */
    AtomicReference<String> justThrewUpALittleInMyMouthAppointmentId = new AtomicReference<>("");
    verifyAll(
        test(
            200,
            Appointment.Bundle.class,
            bundle -> {
              /* Excuse me while I abuse this validation predicate to steal an ID. */
              Safe.stream(bundle.entry())
                  .map(AbstractEntry::resource)
                  .filter(Objects::nonNull)
                  .map(Resource::id)
                  .filter(Objects::nonNull)
                  .findFirst()
                  .ifPresent(justThrewUpALittleInMyMouthAppointmentId::set);
              return true;
            },
            "Appointment?patient={icn}",
            testIds.patient()),
        test(
            200,
            Appointment.Bundle.class,
            "Appointment?date=ge{daysGoneBy}&date=le{daysOfFuturePast}&patient={icn}",
            Year.now().minusYears(10).toString(),
            Year.now().plusYears(1).toString(),
            testIds.patient()),
        test(
            400,
            OperationOutcome.class,
            "Appointment?date=lt2010&date=ge2012&patient={icn}",
            testIds.patient()));
    log.info("Found appointment ID {}", justThrewUpALittleInMyMouthAppointmentId.get());
    if (isNotBlank(justThrewUpALittleInMyMouthAppointmentId.get())) {
      verifyAll(
          test(
              200,
              Appointment.class,
              "Appointment/{appointment}",
              justThrewUpALittleInMyMouthAppointmentId.get()));
    }
  }
}
