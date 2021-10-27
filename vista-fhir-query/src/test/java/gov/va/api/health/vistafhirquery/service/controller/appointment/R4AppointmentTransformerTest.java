package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import org.junit.jupiter.api.Test;

public class R4AppointmentTransformerTest {

  @Test
  void empty() {
    assertThat(
            R4AppointmentTransformer.builder()
                .patientIcn("p1")
                .site("673")
                .rpcResults(VprGetPatientData.Response.Results.builder().build())
                .build()
                .toFhirSkeleton())
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
                .toFhirSkeleton()
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
