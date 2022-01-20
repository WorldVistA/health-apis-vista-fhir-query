package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import org.junit.jupiter.api.Test;

class R4MedicationDispenseTransformerTest {

  @Test
  void medicationDispenseHasAllFields() {
    assertThat(
            R4MedicationDispenseTransformer.builder()
                .site("673")
                .patientIcn("p1")
                .rpcResults(MedicationDispenseSamples.Vista.create().results())
                .build()
                .toFhir()
                .findFirst()
                .get())
        .isEqualTo(MedicationDispenseSamples.R4.create().medicationDispense());
  }

  @Test
  void medicationDispenseIsEmpty() {
    assertThat(
            R4MedicationDispenseTransformer.builder()
                .site("673")
                .patientIcn("p1")
                .rpcResults(VprGetPatientData.Response.Results.builder().build())
                .build()
                .toFhir())
        .isEmpty();
  }

  @Test
  void medicationDispenseIsNotEmpty() {
    assertThat(
            R4MedicationDispenseTransformer.builder()
                .site("673")
                .patientIcn("p1")
                .rpcResults(MedicationDispenseSamples.Vista.create().results())
                .build()
                .toFhir())
        .isNotEmpty();
  }
}
