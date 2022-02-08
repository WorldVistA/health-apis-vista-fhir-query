package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class R4AppointmentTransformerTest {
  private static Stream<Arguments> serviceCategoryCode() {
    return Stream.of(
        Arguments.of("MEDICINE", "M"),
        Arguments.of("NEUROLOGY", "N"),
        Arguments.of("NONE", "0"),
        Arguments.of("PSYCHIATRY", "P"),
        Arguments.of("REHAB MEDICINE", "R"),
        Arguments.of("SURGERY", "S"),
        Arguments.of("WHODIS", null));
  }

  @Test
  void appointmentStatus() {
    assertThat(tx().appointmentStatus(null, "")).isNull();
    assertThat(
            tx().appointmentStatus(ValueOnlyXmlAttribute.builder().value("CANCELLED").build(), ""))
        .isEqualTo(Appointment.AppointmentStatus.cancelled);
    assertThat(
            tx().appointmentStatus(
                    ValueOnlyXmlAttribute.builder().value("SCHEDULED/KEPT").build(), ""))
        .isEqualTo(Appointment.AppointmentStatus.booked);
    assertThat(
            tx().appointmentStatus(
                    ValueOnlyXmlAttribute.builder().value("NO ACTION TAKEN").build(), ""))
        .isEqualTo(Appointment.AppointmentStatus.booked);
  }

  @Test
  void appointmentTypeReturnsNullWhenNull() {
    assertThat(tx().appointmentType(null)).isNull();
  }

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

  @ParameterizedTest
  @MethodSource
  void serviceCategoryCode(String display, String code) {
    assertThat(tx().serviceCategoryCode(display)).isEqualTo(code);
  }

  @Test
  void serviceCategoryIsNullWhenBlank() {
    assertThat(tx().serviceCategory(null)).isNull();
    assertThat(tx().serviceCategory(ValueOnlyXmlAttribute.builder().build())).isNull();
  }

  @Test
  void serviceTypeIsNullWhenBlank() {
    assertThat(tx().serviceType(null)).isNull();
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
