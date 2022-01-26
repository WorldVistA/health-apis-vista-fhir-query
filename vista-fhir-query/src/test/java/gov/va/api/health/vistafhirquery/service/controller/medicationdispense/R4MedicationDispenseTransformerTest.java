package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.controller.medicationdispense.R4MedicationDispenseTransformer.category;
import static gov.va.api.health.vistafhirquery.service.controller.medicationdispense.R4MedicationDispenseTransformer.r4PrescriptionStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.MedicationDispense.Status;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
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
  void r4CategoryTest() {
    assertThat(category("I"))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .code("inpatient")
                            .display("Inpatient")
                            .system(
                                "http://terminology.hl7.org/fhir/CodeSystem/medicationdispense-category")
                            .build()))
                .build());
    assertThat(category("O"))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .code("outpatient")
                            .display("Outpatient")
                            .system(
                                "http://terminology.hl7.org/fhir/CodeSystem/medicationdispense-category")
                            .build()))
                .build());
    assertThat(category("N"))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .code("outpatient")
                            .display("Outpatient")
                            .system(
                                "http://terminology.hl7.org/fhir/CodeSystem/medicationdispense-category")
                            .build()))
                .build());
    assertThat(category("V"))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .code("inpatient")
                            .display("Inpatient")
                            .system(
                                "http://terminology.hl7.org/fhir/CodeSystem/medicationdispense-category")
                            .build()))
                .build());
    assertThrows(
        IllegalStateException.class,
        () -> {
          category("X");
        });
  }

  @Test
  void r4PrescriptionStatusTest() {
    assertThat(r4PrescriptionStatus("HOLD")).isEqualTo(Status.in_progress);
    assertThat(r4PrescriptionStatus("PROVIDER HOLD")).isEqualTo(Status.in_progress);
    assertThat(r4PrescriptionStatus("ACTIVE")).isEqualTo(Status.in_progress);
    assertThat(r4PrescriptionStatus("SUSPENDED")).isEqualTo(Status.in_progress);
    assertThat(r4PrescriptionStatus("DRUG INTERACTIONS")).isEqualTo(Status.preparation);
    assertThat(r4PrescriptionStatus("NON VERIFIED")).isEqualTo(Status.preparation);
    assertThat(r4PrescriptionStatus("DISCONTINUED")).isEqualTo(Status.stopped);
    assertThat(r4PrescriptionStatus("DISCONTINUED (EDIT)")).isEqualTo(Status.stopped);
    assertThat(r4PrescriptionStatus("DISCONTINUED BY PROVIDER")).isEqualTo(Status.stopped);
    assertThat(r4PrescriptionStatus("DELETED")).isEqualTo(Status.entered_in_error);
    assertThat(r4PrescriptionStatus("EXPIRED")).isEqualTo(Status.completed);
    assertThrows(
        IllegalStateException.class,
        () -> {
          r4PrescriptionStatus("NOVAL");
        });
  }
}
