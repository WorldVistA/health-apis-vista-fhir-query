package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

public class R4AppointmentTransformerTest {

  @Test
  void bookedOrFulfilled() {
    assertThat(tx().bookedOrFulfilledStatus(null)).isEqualTo(Appointment.AppointmentStatus.booked);

    assertThat(tx().bookedOrFulfilledStatus(Instant.now().minus(14, ChronoUnit.DAYS).toString()))
        .isEqualTo(Appointment.AppointmentStatus.fulfilled);

    assertThat(tx().bookedOrFulfilledStatus(Instant.now().plus(14, ChronoUnit.DAYS).toString()))
        .isEqualTo(Appointment.AppointmentStatus.booked);
  }

  @Test
  void empty() {
    assertThat(
            R4AppointmentTransformer.builder()
                .patientIcn("p1")
                .site("673")
                .rpcResults(VprGetPatientData.Response.Results.builder().build())
                .build()
                .toFhir())
        .isEmpty();
  }

  @Test
  void idFrom() {
    assertThat(tx().idFrom(null)).isNull();
    assertThat(tx().idFrom("")).isNull();
    assertThat(tx().idFrom("p1")).isEqualTo("sNp1+123+Ap1");
  }

  @Test
  void toFhir() {
    assertThat(
            R4AppointmentTransformer.builder()
                .patientIcn("p1")
                .site("673")
                .rpcResults(AppointmentSamples.Vista.create().results())
                .build()
                .toFhir()
                .findFirst()
                .get())
        .isEqualTo(AppointmentSamples.R4.create().appointment());
  }

  private R4AppointmentTransformer tx() {
    return R4AppointmentTransformer.builder()
        .patientIcn("p1")
        .site("123")
        .rpcResults(VprGetPatientData.Response.Results.builder().build())
        .build();
  }
}
