package gov.va.api.health.vistafhirquery.service.controller.condition;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.charon.models.CodeAndNameXmlAttribute;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Problems;
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
  void idFrom() {
    assertThat(tx().idFrom(null)).isNull();
    assertThat(tx().idFrom(ValueOnlyXmlAttribute.of(""))).isNull();
    assertThat(tx().idFrom(ValueOnlyXmlAttribute.of("p1"))).isEqualTo("sNp1+123+Pp1");
  }

  @Test
  void nullSafe() {
    assertThat(tx().toFhir().toList())
        .isEqualTo(
            Condition.builder()
                .meta(Meta.builder().source("123").build())
                .subject(Reference.builder().reference("Patient/p1").build())
                .category(
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
                        .asList())
                .build()
                .asList());
  }

  @Test
  void toFhir() {
    assertThat(
            VistaProblemToR4ConditionTransformer.builder()
                .patientIcn("p1")
                .site("123")
                .vistaProblem(ConditionProblemListSamples.Vista.create().problem("p1"))
                .build()
                .toFhir()
                .findFirst()
                .get())
        .isEqualTo(ConditionProblemListSamples.R4.create().condition("sNp1+123+Pp1"));
  }

  private VistaProblemToR4ConditionTransformer tx() {
    return VistaProblemToR4ConditionTransformer.builder()
        .patientIcn("p1")
        .site("123")
        .vistaProblem(Problems.Problem.builder().build())
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
