package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.codeableconceptHasCodingSystem;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.extensionForSystem;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.identifierHasCodingSystem;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.recordCoordinatesForReference;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4InsurancePlanToGroupInsurancePlanFileTransformer {
  @NonNull InsurancePlan insurancePlan;

  @Builder
  R4InsurancePlanToGroupInsurancePlanFileTransformer(@NonNull InsurancePlan insurancePlan) {
    this.insurancePlan = insurancePlan;
  }

  WriteableFilemanValue electronicPlanType(List<CodeableConcept> codeableConcepts) {
    return extractFromList(
        "plan",
        codeableConcepts,
        c -> codeableconceptHasCodingSystem(c, "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8015"),
        this::extractCodeFromCoding,
        GroupInsurancePlan.ELECTRONIC_PLAN_TYPE,
        "system type 'urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8015' not found");
  }

  @SuppressWarnings("DoNotCallSuggester")
  void extensionNotFound(String fieldName) {
    throw ResourceExceptions.BadRequestPayload.because("extension not found: " + fieldName);
  }

  WriteableFilemanValue extensionToBooleanWriteableFilemanValue(Extension extension, String field) {
    if (isBlank(extension) || isBlank(extension.valueBoolean())) {
      throw ResourceExceptions.BadRequestPayload.because(
          field, "extension is null or has no boolean value");
    }
    return groupInsurancePlanCoordinates(field, 1, extension.valueBoolean() ? "YES" : "NO");
  }

  String extractCodeFromCoding(CodeableConcept c) {
    return c.coding().stream().findFirst().map(Coding::code).orElse("");
  }

  <T, R> WriteableFilemanValue extractFromList(
      String parameterName,
      List<T> object,
      Predicate<? super T> predicate,
      Function<? super T, ? extends R> mapper,
      String field,
      String error) {
    if (isBlank(object)) {
      throw ResourceExceptions.BadRequestPayload.because(field, parameterName + " is null");
    }
    return object.stream()
        .filter(predicate)
        .map(mapper)
        .findFirst()
        .map(value -> groupInsurancePlanCoordinates(field, 1, (String) value))
        .orElseThrow(() -> ResourceExceptions.BadRequestPayload.because(field, error));
  }

  private WriteableFilemanValue groupInsurancePlanCoordinates(
      String field, Integer index, String value) {
    return WriteableFilemanValue.builder()
        .file(GroupInsurancePlan.FILE_NUMBER)
        .field(field)
        .index(index)
        .value(value)
        .build();
  }

  WriteableFilemanValue groupName(String name) {
    if (isBlank(name)) {
      throw ResourceExceptions.BadRequestPayload.because("name is null");
    }
    return groupInsurancePlanCoordinates(GroupInsurancePlan.GROUP_NAME, 1, name);
  }

  WriteableFilemanValue groupNumber(List<Identifier> identifiers) {
    return extractFromList(
        "identifiers",
        identifiers,
        i -> identifierHasCodingSystem(i, "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002"),
        Identifier::value,
        GroupInsurancePlan.GROUP_NUMBER,
        "system type 'urn:oid:2.16.840.1.113883.3.8901.3.1.355803.28002' not found");
  }

  WriteableFilemanValue insuranceCompany(Reference reference) {
    if (isBlank(reference) || isBlank(reference.reference())) {
      throw ResourceExceptions.BadRequestPayload.because(
          GroupInsurancePlan.INSURANCE_COMPANY, "ownedBy is null");
    }
    var recordCoordinates = recordCoordinatesForReference(reference);
    return recordCoordinates.isEmpty()
        ? null
        : pointer(InsuranceCompany.FILE_NUMBER, 1, recordCoordinates.get().ien());
  }

  WriteableFilemanValue planId(List<Identifier> identifiers) {
    return extractFromList(
        "identifiers",
        identifiers,
        i -> identifierHasCodingSystem(i, "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001"),
        Identifier::value,
        GroupInsurancePlan.PLAN_ID,
        "system type 'urn:oid:2.16.840.1.113883.3.8901.3.1.355803.68001' not found");
  }

  List<WriteableFilemanValue> planStandardFtf(Extension extension) {
    if (isBlank(extension) || isBlank(extension.valueQuantity())) {
      throw ResourceExceptions.BadRequestPayload.because(
          GroupInsurancePlan.PLAN_STANDARD_FTF, "extension is null or has no valueQuantity");
    }
    return List.of(
        groupInsurancePlanCoordinates(
            GroupInsurancePlan.PLAN_STANDARD_FTF, 1, extension.valueQuantity().unit()),
        groupInsurancePlanCoordinates(
            GroupInsurancePlan.PLAN_STANDARD_FTF_VALUE,
            1,
            extension.valueQuantity().value().toString()));
  }

  private WriteableFilemanValue pointer(String fileNumber, int index, String ien) {
    if (isBlank(ien)) {
      return null;
    }
    return WriteableFilemanValue.builder()
        .file(fileNumber)
        .field("ien")
        .index(index)
        .value(ien)
        .build();
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toGroupInsurancePlanFile() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    fields.add(insuranceCompany(insurancePlan().ownedBy()));
    extensionForSystem(
            insurancePlan().extension(),
            "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired")
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(
                    e, GroupInsurancePlan.IS_UTILIZATION_REVIEW_REQUIRED))
        .ifPresentOrElse(
            fields::add,
            () -> extensionNotFound(GroupInsurancePlan.IS_UTILIZATION_REVIEW_REQUIRED));
    extensionForSystem(
            insurancePlan().extension(),
            "http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired")
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(
                    e, GroupInsurancePlan.IS_PRE_CERTIFICATION_REQUIRED_))
        .ifPresentOrElse(
            fields::add,
            () -> extensionNotFound(GroupInsurancePlan.IS_PRE_CERTIFICATION_REQUIRED_));
    extensionForSystem(
            insurancePlan().extension(),
            "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions")
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(
                    e, GroupInsurancePlan.EXCLUDE_PRE_EXISTING_CONDITION))
        .ifPresentOrElse(
            fields::add,
            () -> extensionNotFound(GroupInsurancePlan.EXCLUDE_PRE_EXISTING_CONDITION));
    extensionForSystem(
            insurancePlan().extension(),
            "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable")
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(e, GroupInsurancePlan.BENEFITS_ASSIGNABLE_))
        .ifPresentOrElse(
            fields::add, () -> extensionNotFound(GroupInsurancePlan.BENEFITS_ASSIGNABLE_));
    fields.add(typeOfPlan(insurancePlan().plan()));
    extensionForSystem(
            insurancePlan().extension(),
            "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable")
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(
                    e, GroupInsurancePlan.AMBULATORY_CARE_CERTIFICATION))
        .ifPresentOrElse(
            fields::add, () -> extensionNotFound(GroupInsurancePlan.AMBULATORY_CARE_CERTIFICATION));
    fields.add(electronicPlanType(insurancePlan().type()));
    extensionForSystem(
            insurancePlan().extension(),
            "http://va.gov/fhir/StructureDefinition/insuranceplan-planStandardFilingTimeFrame")
        .map(this::planStandardFtf)
        .ifPresentOrElse(
            fields::addAll, () -> extensionNotFound(GroupInsurancePlan.PLAN_STANDARD_FTF));
    fields.add(groupName(insurancePlan().name()));
    fields.add(groupNumber(insurancePlan().identifier()));
    fields.add(planId(insurancePlan().identifier()));
    return fields;
  }

  WriteableFilemanValue typeOfPlan(List<InsurancePlan.Plan> plan) {
    return extractFromList(
        "plan",
        plan,
        c ->
            codeableconceptHasCodingSystem(
                c.type(), "urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009"),
        c -> extractCodeFromCoding(c.type()),
        GroupInsurancePlan.TYPE_OF_PLAN,
        "system type 'urn:oid:2.16.840.1.113883.3.8901.3.1.355803.8009' not found");
  }
}
