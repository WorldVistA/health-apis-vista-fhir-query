package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.valueOfValueOnlyXmlAttribute;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.charon.models.ValueOnlyXmlAttribute;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Problems;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class VistaProblemToR4ConditionTransformer {
  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull VprGetPatientData.Response.Results rpcResults;

  /** Fixed value of problem-list-item for data from Problem domain. */
  List<CodeableConcept> category() {
    return CodeableConcept.builder()
        .coding(
            Coding.builder()
                .code("problem-list-item")
                .display("Problem List Item")
                .system("http://terminology.hl7.org/CodeSystem/condition-category")
                .build()
                .asList())
        .text("Problem List Item")
        .build()
        .asList();
  }

  CodeableConcept clinicalStatus(Problems.Problem rpcCondition) {
    if (!isBlank(toHumanDateTime(rpcCondition.resolved()))) {
      return CodeableConcept.builder()
          .text("Resolved")
          .coding(
              Coding.builder()
                  .system("http://terminology.hl7.org/CodeSystem/condition-clinical")
                  .code("resolved")
                  .display("Resolved")
                  .build()
                  .asList())
          .build();
    }
    var status = rpcCondition.status();
    if (isBlank(status)) {
      return null;
    }
    if ("A".equals(status.code())) {
      return CodeableConcept.builder()
          .text("Active")
          .coding(
              Coding.builder()
                  .system("http://terminology.hl7.org/CodeSystem/condition-clinical")
                  .code("active")
                  .display("Active")
                  .build()
                  .asList())
          .build();
    }
    if ("I".equals(status.code())) {
      return CodeableConcept.builder()
          .text("Inactive")
          .coding(
              Coding.builder()
                  .system("http://terminology.hl7.org/CodeSystem/condition-clinical")
                  .code("inactive")
                  .display("Inactive")
                  .build()
                  .asList())
          .build();
    }
    return null;
  }

  CodeableConcept code(Problems.Problem rpcCondition) {
    List<Coding> codings = new ArrayList<>();
    String display = null;
    String icdSystem = null;
    String codingSystemValue = valueOfValueOnlyXmlAttribute(rpcCondition.codingSystem());
    String icdValue = valueOfValueOnlyXmlAttribute(rpcCondition.icd());
    String icddValue = valueOfValueOnlyXmlAttribute(rpcCondition.icdd());
    if ("ICD".equals(codingSystemValue)) {
      display = icddValue;
      icdSystem = "http://hl7.org/fhir/sid/icd-9-cm";
    }
    if ("10D".equals(codingSystemValue)) {
      display = icddValue;
      icdSystem = "http://hl7.org/fhir/sid/icd-10-cm";
    }
    if (!isBlank(icdSystem)) {
      codings.add(Coding.builder().system(icdSystem).code(icdValue).display(display).build());
    }

    String sctcValue = valueOfValueOnlyXmlAttribute(rpcCondition.sctc());
    String scttValue = valueOfValueOnlyXmlAttribute(rpcCondition.sctt());
    if (!isBlank(sctcValue)) {
      display = scttValue;
      codings.add(
          Coding.builder()
              .system("http://snomed.info/sct")
              .code(sctcValue)
              .display(display)
              .build());
    }
    return CodeableConcept.builder().coding(codings).text(display).build();
  }

  String idFrom(String id) {
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, VprGetPatientData.Domains.problems, id);
  }

  private Condition toCondition(Problems.Problem rpcCondition) {
    if (isBlank(rpcCondition)) {
      return null;
    }
    return Condition.builder()
        .id(idFrom(rpcCondition.id().value()))
        .meta(
            Meta.builder()
                .source(site)
                .lastUpdated(toHumanDateTime(rpcCondition.updated()))
                .build())
        .clinicalStatus(clinicalStatus(rpcCondition))
        .category(category())
        .code(code(rpcCondition))
        .subject(toReference("Patient", patientIcn, null))
        .verificationStatus(verificationStatus(rpcCondition.unverified()))
        .onsetDateTime(toHumanDateTime(rpcCondition.onset()))
        .recordedDate(toHumanDateTime(rpcCondition.entered()))
        .build();
  }

  Stream<Condition> toFhir() {
    return rpcResults.problemStream().map(this::toCondition).filter(Objects::nonNull);
  }

  CodeableConcept verificationStatus(ValueOnlyXmlAttribute unverified) {
    String unverifiedValue = valueOfValueOnlyXmlAttribute(unverified);
    if ("0".equals(unverifiedValue)) {
      return CodeableConcept.builder()
          .coding(
              Coding.builder()
                  .system("http://terminology.hl7.org/CodeSystem/condition-ver-status")
                  .code("confirmed")
                  .display("Confirmed")
                  .build()
                  .asList())
          .text("Confirmed")
          .build();
    }
    if ("1".equals(unverifiedValue)) {
      return CodeableConcept.builder()
          .coding(
              Coding.builder()
                  .system("http://terminology.hl7.org/CodeSystem/condition-ver-status")
                  .code("unconfirmed")
                  .display("Unconfirmed")
                  .build()
                  .asList())
          .text("Unconfirmed")
          .build();
    }
    return null;
  }
}
