package gov.va.api.health.vistafhirquery.service.controller.medicationrequest;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.MedicationRequest;
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
  void toFhir() {
    assertThat(_transform(MedicationRequestSamples.Vista.create().results()).findFirst().get())
        .isEqualTo(MedicationRequestSamples.R4.create().medicationRequest());
  }
}
