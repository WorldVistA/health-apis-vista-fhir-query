package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.optionalInstantToString;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toResourceId;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Visits;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class VprVisitToR4ConditionTransformer {
  @NonNull String site;

  @NonNull String patientIcn;

  @NonNull Visits.Visit vistaVisit;

  /** Fixed value of encounter-diagnosis for data from Visit domain. */
  List<CodeableConcept> category() {
    return CodeableConcept.builder()
        .coding(
            Coding.builder()
                .code("encounter-diagnosis")
                .display("Encounter Diagnosis")
                .system("http://terminology.hl7.org/CodeSystem/condition-category")
                .build()
                .asList())
        .text("Encounter Diagnosis")
        .build()
        .asList();
  }

  CodeableConcept code(Visits.Icd icd) {
    if (icd == null) {
      return null;
    }
    var coding = icdToCoding(icd);
    if (coding == null) {
      return null;
    }
    return CodeableConcept.builder().coding(coding.asList()).build();
  }

  private Coding icdToCoding(Visits.Icd icd) {
    String icdSystem;
    String codingSystemValue = icd.system();
    if (codingSystemValue == null) {
      return null;
    }
    switch (codingSystemValue) {
      case "ICD":
        icdSystem = "http://hl7.org/fhir/sid/icd-9-cm";
        break;
      case "10D":
        icdSystem = "http://hl7.org/fhir/sid/icd-10-cm";
        break;
      default:
        throw new IllegalArgumentException("ICD system not supported: " + codingSystemValue);
    }
    if (isBlank(icd.code())) {
      return null;
    }
    return Coding.builder().system(icdSystem).code(icd.code()).display(icd.narrative()).build();
  }

  private List<Condition> icdToCondition(Visits.Visit rpcCondition) {
    if (isBlank(rpcCondition.icd())) {
      return null;
    }
    return rpcCondition.icd().stream()
        .map(icd -> toCondition(rpcCondition, icd))
        .filter(Objects::nonNull)
        .collect(toList());
  }

  String idFrom(String id) {
    if (isBlank(id)) {
      return null;
    }
    return toResourceId(patientIcn, site, Domains.visits, id);
  }

  private Condition toCondition(Visits.Visit rpcCondition, Visits.Icd icd) {
    return Condition.builder()
        .id(idFrom(rpcCondition.id().value()))
        .meta(Meta.builder().source(site).build())
        .category(category())
        .code(code(icd))
        .subject(toReference("Patient", patientIcn, null))
        .recordedDate(optionalInstantToString(toHumanDateTime(rpcCondition.dateTime())))
        .build();
  }

  Stream<Condition> toFhir() {
    return Stream.of(vistaVisit)
        .map(this::icdToCondition)
        .filter(Objects::nonNull)
        .flatMap(Collection::stream);
  }
}
