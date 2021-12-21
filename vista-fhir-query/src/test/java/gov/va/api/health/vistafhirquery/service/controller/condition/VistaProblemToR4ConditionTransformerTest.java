package gov.va.api.health.vistafhirquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Problems;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VistaProblemToR4ConditionTransformerTest {
  @Test
  void categoryIsFixedProblemItemList() {
    assertThat(tx().category())
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    Coding.builder()
                        .code("problem-list-item")
                        .display("Problem List Item")
                        .system("http://terminology.hl7.org/CodeSystem/condition-category")
                        .build()
                        .asList())
                .text("Problem List Item")
                .build()
                .asList());
  }

  @Test
  void clinicalStatus() {
    CodeableConcept activeClinicalStatus =
        CodeableConcept.builder()
            .text("Active")
            .coding(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/condition-clinical")
                    .code("active")
                    .display("Active")
                    .build()
                    .asList())
            .build();
    CodeableConcept inactiveClinicalStatus =
        CodeableConcept.builder()
            .text("Inactive")
            .coding(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/condition-clinical")
                    .code("inactive")
                    .display("Inactive")
                    .build()
                    .asList())
            .build();
    CodeableConcept resolvedClinicalStatus =
        CodeableConcept.builder()
            .text("Resolved")
            .coding(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/condition-clinical")
                    .code("resolved")
                    .display("Resolved")
                    .build()
                    .asList())
            .build();
    assertThat(
            tx().clinicalStatus(
                    Problems.Problem.builder()
                        .resolved(ValueOnlyXmlAttribute.of("3331212"))
                        .build()))
        .isEqualTo(resolvedClinicalStatus);
    assertThat(
            tx().clinicalStatus(
                    Problems.Problem.builder()
                        .resolved(ValueOnlyXmlAttribute.of("3331212"))
                        .status(CodeAndNameXmlAttribute.of("A", "ACTIVE"))
                        .build()))
        .isEqualTo(resolvedClinicalStatus);
    assertThat(
            tx().clinicalStatus(
                    Problems.Problem.builder()
                        .status(CodeAndNameXmlAttribute.of("A", "ACTIVE"))
                        .build()))
        .isEqualTo(activeClinicalStatus);
    assertThat(
            tx().clinicalStatus(
                    Problems.Problem.builder()
                        .status(CodeAndNameXmlAttribute.of("I", "INACTIVE"))
                        .build()))
        .isEqualTo(inactiveClinicalStatus);
    assertThat(
            tx().clinicalStatus(
                    Problems.Problem.builder()
                        .status(CodeAndNameXmlAttribute.of("?", "???"))
                        .build()))
        .isNull();
  }

  @Test
  void code() {
    assertThat(
            tx().code(
                    Problems.Problem.builder()
                        .sctt(ValueOnlyXmlAttribute.of("a"))
                        .sctc(ValueOnlyXmlAttribute.of("b"))
                        .build()))
        .isEqualTo(
            CodeableConcept.builder()
                .text("a")
                .coding(
                    Coding.builder()
                        .display("a")
                        .code("b")
                        .system("http://snomed.info/sct")
                        .build()
                        .asList())
                .build());
    assertThat(
            tx().code(
                    Problems.Problem.builder()
                        .sctt(ValueOnlyXmlAttribute.of("a"))
                        .sctc(ValueOnlyXmlAttribute.of("b"))
                        .icd(ValueOnlyXmlAttribute.of("c"))
                        .icdd(ValueOnlyXmlAttribute.of("d"))
                        .codingSystem(ValueOnlyXmlAttribute.of("10D"))
                        .build()))
        .isEqualTo(
            CodeableConcept.builder()
                .text("a")
                .coding(
                    List.of(
                        Coding.builder()
                            .code("c")
                            .display("d")
                            .system("http://hl7.org/fhir/sid/icd-10-cm")
                            .build(),
                        Coding.builder()
                            .display("a")
                            .code("b")
                            .system("http://snomed.info/sct")
                            .build()))
                .build());
  }

  @Test
  void empty() {
    assertThat(
            VistaProblemToR4ConditionTransformer.builder()
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
    assertThat(tx().idFrom("p1")).isEqualTo("sNp1+123+Pp1");
  }

  @Test
  void toFhir() {
    assertThat(
            VistaProblemToR4ConditionTransformer.builder()
                .patientIcn("p1")
                .site("673")
                .rpcResults(ConditionProblemListSamples.Vista.create().results())
                .build()
                .toFhir()
                .findFirst()
                .get())
        .isEqualTo(ConditionProblemListSamples.R4.create().condition());
  }

  private VistaProblemToR4ConditionTransformer tx() {
    return VistaProblemToR4ConditionTransformer.builder()
        .patientIcn("p1")
        .site("123")
        .rpcResults(VprGetPatientData.Response.Results.builder().build())
        .build();
  }

  @Test
  void verificationStatus() {
    assertThat(tx().verificationStatus(null)).isNull();
    assertThat(tx().verificationStatus(ValueOnlyXmlAttribute.of(null))).isNull();
    assertThat(tx().verificationStatus(ValueOnlyXmlAttribute.of(""))).isNull();
    assertThat(tx().verificationStatus(ValueOnlyXmlAttribute.of("bogusvalue"))).isNull();
    assertThat(tx().verificationStatus(ValueOnlyXmlAttribute.of("0")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    Coding.builder()
                        .system("http://terminology.hl7.org/CodeSystem/condition-ver-status")
                        .code("confirmed")
                        .display("Confirmed")
                        .build()
                        .asList())
                .text("Confirmed")
                .build());
    assertThat(tx().verificationStatus(ValueOnlyXmlAttribute.of("1")))
        .isEqualTo(
            CodeableConcept.builder()
                .coding(
                    Coding.builder()
                        .system("http://terminology.hl7.org/CodeSystem/condition-ver-status")
                        .code("unconfirmed")
                        .display("Unconfirmed")
                        .build()
                        .asList())
                .text("Unconfirmed")
                .build());
  }
}
