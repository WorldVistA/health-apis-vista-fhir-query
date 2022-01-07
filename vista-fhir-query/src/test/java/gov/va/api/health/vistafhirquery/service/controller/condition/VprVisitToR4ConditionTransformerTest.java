package gov.va.api.health.vistafhirquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Visits;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VprVisitToR4ConditionTransformerTest {

  @Test
  void code() {
    assertThat(
            tx().code(
                    List.of(
                        Visits.Icd.builder()
                            .code("c")
                            .name("a")
                            .system("ICD")
                            .narrative("a")
                            .ranking("P")
                            .build())))
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
                    List.of(
                        Visits.Icd.builder()
                            .code("c")
                            .name("a")
                            .system("10D")
                            .narrative("a")
                            .ranking("P")
                            .build())))
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
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                tx().code(
                        List.of(
                            Visits.Icd.builder()
                                .code("c")
                                .name("a")
                                .system("NOPE")
                                .narrative("a")
                                .ranking("P")
                                .build())));
  }

  @Test
  void empty() {
    assertThat(
            VprVisitToR4ConditionTransformer.builder()
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
    assertThat(tx().idFrom("p1")).isEqualTo("sNp1+123+Tp1");
  }

  @Test
  void toFhir() {
    assertThat(
            VprVisitToR4ConditionTransformer.builder()
                .patientIcn("p1")
                .site("673")
                .rpcResults(ConditionEncounterDiagnosisSamples.Vista.create().results())
                .build()
                .toFhir()
                .findFirst()
                .get())
        .isEqualTo(ConditionEncounterDiagnosisSamples.R4.create().condition());
  }

  private VprVisitToR4ConditionTransformer tx() {
    return VprVisitToR4ConditionTransformer.builder()
        .patientIcn("p1")
        .site("123")
        .rpcResults(VprGetPatientData.Response.Results.builder().build())
        .build();
  }
}
