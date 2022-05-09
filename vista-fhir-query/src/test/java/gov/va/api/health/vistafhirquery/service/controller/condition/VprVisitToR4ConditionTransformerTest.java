package gov.va.api.health.vistafhirquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Visits;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VprVisitToR4ConditionTransformerTest {

  @Test
  void categoryIsFixedEncounterDiagnosisList() {
    assertThat(tx().category())
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    Coding.builder()
                        .code("encounter-diagnosis")
                        .display("Encounter Diagnosis")
                        .system("http://terminology.hl7.org/CodeSystem/condition-category")
                        .build()
                        .asList())
                .text("Encounter Diagnosis")
                .build()
                .asList());
  }

  @Test
  void code() {
    assertThat(
            tx().code(
                    Visits.Icd.builder()
                        .code("c")
                        .name("a")
                        .system("ICD")
                        .narrative("a")
                        .ranking("P")
                        .build()))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .code("c")
                            .display("a")
                            .system("http://hl7.org/fhir/sid/icd-9-cm")
                            .build()))
                .build());
    assertThat(
            tx().code(
                    Visits.Icd.builder()
                        .code("c")
                        .name("a")
                        .system("10D")
                        .narrative("a")
                        .ranking("P")
                        .build()))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .code("c")
                            .display("a")
                            .system("http://hl7.org/fhir/sid/icd-10-cm")
                            .build()))
                .build());
    // null icd
    assertThat(tx().code(null)).isNull();
    // null system
    assertThat(
            tx().code(Visits.Icd.builder().code("c").name("a").narrative("a").ranking("P").build()))
        .isNull();
    // null code
    assertThat(
            tx().code(
                    Visits.Icd.builder()
                        .name("a")
                        .system("10D")
                        .narrative("a")
                        .ranking("P")
                        .build()))
        .isNull();
    // unknown system
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                tx().code(
                        Visits.Icd.builder()
                            .code("c")
                            .name("a")
                            .system("NOPE")
                            .narrative("a")
                            .ranking("P")
                            .build()));
  }

  @Test
  void idFrom() {
    assertThat(tx().idFrom(null, "100")).isNull();
    assertThat(tx().idFrom("p1", "")).isNull();
    assertThat(tx().idFrom("p1", "100.1")).isEqualTo("sNp1-123-Tp1-100.1");
  }

  @Test
  void noIcds() {
    assertThat(tx().toFhir().toList()).isEmpty();
  }

  @Test
  void toFhir() {
    assertThat(
            VprVisitToR4ConditionTransformer.builder()
                .patientIcn("p1")
                .site("123")
                .vistaVisit(ConditionEncounterDiagnosisSamples.Vista.create().visit("v1"))
                .build()
                .toFhir()
                .findFirst()
                .get())
        .isEqualTo(ConditionEncounterDiagnosisSamples.R4.create().condition("sNp1-123-Tv1-391.2"));
  }

  private VprVisitToR4ConditionTransformer tx() {
    return VprVisitToR4ConditionTransformer.builder()
        .patientIcn("p1")
        .site("123")
        .vistaVisit(Visits.Visit.builder().build())
        .build();
  }
}
