package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.r4.api.resources.MedicationRequest;
import gov.va.api.health.r4.api.resources.MedicationRequest.Status;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class R4MedicationRequestTransformerTest {
  private Stream<MedicationRequest> _transform(VprGetPatientData.Response.Results results) {
    return R4MedicationRequestTransformer.builder()
        .site("673")
        .patientIcn("p1")
        .rpcResults(results)
        .build()
        .toFhir();
  }

  @Test
  void empty() {
    assertThat(_transform(VprGetPatientData.Response.Results.builder().build())).isEmpty();
  }

  @Test
  void status() {
    R4MedicationRequestTransformer tx =
        R4MedicationRequestTransformer.builder()
            .site("673")
            .patientIcn("p1")
            .rpcResults(MedicationRequestSamples.Vista.create().results())
            .build();
    assertThat(tx.status("HOLD")).isEqualTo(Status.active);
    assertThat(tx.status("PROVIDER HOLD")).isEqualTo(Status.active);
    assertThat(tx.status("DRUG INTERACTIONS")).isEqualTo(Status.draft);
    assertThat(tx.status("NON-VERIFIED")).isEqualTo(Status.draft);
    assertThat(tx.status("ACTIVE")).isEqualTo(Status.active);
    assertThat(tx.status("SUSPENDED")).isEqualTo(Status.active);
    assertThat(tx.status("DISCONTINUED")).isEqualTo(Status.stopped);
    assertThat(tx.status("DISCONTINUED (EDIT)")).isEqualTo(Status.stopped);
    assertThat(tx.status("DISCONTINUED BY PROVIDER")).isEqualTo(Status.stopped);
    assertThat(tx.status("DELETED")).isEqualTo(Status.entered_in_error);
    assertThat(tx.status("EXPIRED")).isEqualTo(Status.completed);
    assertThat(tx.status(null)).isEqualTo(Status.unknown);
    assertThrows(IllegalStateException.class, () -> tx.status("NOT AN ACTUAL STATUS"));
  }

  @Test
  void toFhir() {
    assertThat(_transform(MedicationRequestSamples.Vista.create().results()).findFirst().get())
        .isEqualTo(MedicationRequestSamples.R4.create().medicationRequest());
  }
}
