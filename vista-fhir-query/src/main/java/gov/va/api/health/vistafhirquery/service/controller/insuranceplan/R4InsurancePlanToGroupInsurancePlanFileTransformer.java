package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.codeableconceptHasCodingSystem;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.extensionForSystem;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.identifierHasCodingSystem;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.recordCoordinatesForReference;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static java.util.function.Function.identity;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4InsurancePlanToGroupInsurancePlanFileTransformer {
  private static final WriteableFilemanValueFactory filemanFactory =
      WriteableFilemanValueFactory.forFile(GroupInsurancePlan.FILE_NUMBER);

  @NonNull InsurancePlan insurancePlan;

  @Builder
  R4InsurancePlanToGroupInsurancePlanFileTransformer(@NonNull InsurancePlan insurancePlan) {
    this.insurancePlan = insurancePlan;
  }

  Optional<WriteableFilemanValue> bankingIdentificationNumber(List<Identifier> identifiers) {
    return extractFromList(
        identifiers,
        i ->
            identifierHasCodingSystem(
                i, InsurancePlanStructureDefinitions.BANKING_IDENTIFICATION_NUMBER),
        Identifier::value,
        GroupInsurancePlan.BANKING_IDENTIFICATION_NUMBER);
  }

  WriteableFilemanValue electronicPlanType(List<CodeableConcept> codeableConcepts) {
    return extractFromListOrDie(
        "plan",
        codeableConcepts,
        c ->
            codeableconceptHasCodingSystem(
                c, InsurancePlanStructureDefinitions.ELECTRONIC_PLAN_TYPE),
        this::extractCodeFromCoding,
        GroupInsurancePlan.ELECTRONIC_PLAN_TYPE,
        InsurancePlanStructureDefinitions.ELECTRONIC_PLAN_TYPE + " system type not found");
  }

  @SuppressWarnings("DoNotCallSuggester")
  void extensionNotFound(String fieldName) {
    throw ResourceExceptions.BadRequestPayload.because("extension not found: " + fieldName);
  }

  WriteableFilemanValue extensionToBooleanWriteableFilemanValue(Extension extension, String field) {
    var wfv = filemanFactory.forBoolean(field, 1, extension);
    if (isBlank(wfv)) {
      throw ResourceExceptions.BadRequestPayload.because(
          field, "extension is null or has no boolean value");
    }
    return wfv;
  }

  String extractCodeFromCoding(CodeableConcept c) {
    return c.coding().stream().findFirst().map(Coding::code).orElse("");
  }

  <T, R> Optional<WriteableFilemanValue> extractFromList(
      List<T> object,
      Predicate<? super T> predicate,
      Function<? super T, ? extends R> mapper,
      String field) {
    if (isBlank(object)) {
      return Optional.empty();
    }
    return object.stream()
        .filter(predicate)
        .map(mapper)
        .filter(Objects::nonNull)
        .findFirst()
        .map(filemanFactory.toString(field, index(1), value -> (String) value));
  }

  <T, R> WriteableFilemanValue extractFromListOrDie(
      String parameterName,
      List<T> object,
      Predicate<? super T> predicate,
      Function<? super T, ? extends R> mapper,
      String field,
      String error) {
    if (isBlank(object)) {
      throw ResourceExceptions.BadRequestPayload.because(field, parameterName + " is null");
    }
    return extractFromList(object, predicate, mapper, field)
        .orElseThrow(() -> ResourceExceptions.BadRequestPayload.because(field, error));
  }

  WriteableFilemanValue groupName(String name) {
    if (isBlank(name)) {
      throw ResourceExceptions.BadRequestPayload.because("name is null");
    }
    return filemanFactory.forString(GroupInsurancePlan.GROUP_NAME, 1, name);
  }

  WriteableFilemanValue groupNumber(List<Identifier> identifiers) {
    return extractFromListOrDie(
        "identifiers",
        identifiers,
        i -> identifierHasCodingSystem(i, InsurancePlanStructureDefinitions.GROUP_NUMBER),
        Identifier::value,
        GroupInsurancePlan.GROUP_NUMBER,
        InsurancePlanStructureDefinitions.GROUP_NUMBER + " system type not found");
  }

  Optional<WriteableFilemanValue> insuranceCompany(Reference reference) {
    return recordCoordinatesForReference(reference)
        .map(filemanFactory.recordCoordinatesToPointer(InsuranceCompany.FILE_NUMBER, index(1)));
  }

  Optional<WriteableFilemanValue> insurancePlanIen(String id) {
    return Optional.ofNullable(id)
        .map(RecordCoordinates::fromString)
        .map(filemanFactory.recordCoordinatesToPointer(GroupInsurancePlan.FILE_NUMBER, index(1)));
  }

  Optional<WriteableFilemanValue> planCategory(List<CodeableConcept> codeableConcepts) {
    return codeableConcepts.stream()
        .filter(
            c -> codeableconceptHasCodingSystem(c, InsurancePlanStructureDefinitions.PLAN_CATEGORY))
        .map(this::extractCodeFromCoding)
        .filter(Objects::nonNull)
        .findFirst()
        .map(filemanFactory.toString(GroupInsurancePlan.PLAN_CATEGORY, index(1), identity()));
  }

  WriteableFilemanValue planId(List<Identifier> identifiers) {
    return extractFromListOrDie(
        "identifiers",
        identifiers,
        i -> identifierHasCodingSystem(i, InsurancePlanStructureDefinitions.PLAN_ID),
        Identifier::value,
        GroupInsurancePlan.PLAN_ID,
        InsurancePlanStructureDefinitions.PLAN_ID + " system type not found");
  }

  List<WriteableFilemanValue> planStandardFtf(Extension extension) {
    if (isBlank(extension) || isBlank(extension.valueQuantity())) {
      throw ResourceExceptions.BadRequestPayload.because(
          GroupInsurancePlan.PLAN_STANDARD_FTF, "extension is null or has no valueQuantity");
    }
    if (isBlank(extension.valueQuantity().unit()) || isBlank(extension.valueQuantity().value())) {
      throw ResourceExceptions.BadRequestPayload.because(
          GroupInsurancePlan.PLAN_STANDARD_FTF, "extension unit/value is null");
    }
    return List.of(
        filemanFactory.forString(
            GroupInsurancePlan.PLAN_STANDARD_FTF, 1, extension.valueQuantity().unit()),
        filemanFactory.forString(
            GroupInsurancePlan.PLAN_STANDARD_FTF_VALUE,
            1,
            extension.valueQuantity().value().toString()));
  }

  Optional<WriteableFilemanValue> processorControlNumber(List<Identifier> identifiers) {
    return extractFromList(
        identifiers,
        i ->
            identifierHasCodingSystem(
                i, InsurancePlanStructureDefinitions.PROCESSOR_CONTROL_NUMBER_PCN),
        Identifier::value,
        GroupInsurancePlan.PROCESSOR_CONTROL_NUMBER_PCN_);
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toGroupInsurancePlanFile() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    insurancePlanIen(insurancePlan().id()).ifPresent(fields::add);
    insuranceCompany(insurancePlan().ownedBy())
        .ifPresentOrElse(
            fields::add,
            () -> {
              throw ResourceExceptions.BadRequestPayload.because("ownedBy field not found");
            });
    extensionForSystem(
            insurancePlan().extension(),
            InsurancePlanStructureDefinitions.IS_UTILIZATION_REVIEW_REQUIRED)
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(
                    e, GroupInsurancePlan.IS_UTILIZATION_REVIEW_REQUIRED))
        .ifPresentOrElse(
            fields::add,
            () ->
                extensionNotFound(
                    InsurancePlanStructureDefinitions.IS_UTILIZATION_REVIEW_REQUIRED));
    extensionForSystem(
            insurancePlan().extension(),
            InsurancePlanStructureDefinitions.IS_PRE_CERTIFICATION_REQUIRED)
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(
                    e, GroupInsurancePlan.IS_PRE_CERTIFICATION_REQUIRED_))
        .ifPresentOrElse(
            fields::add,
            () ->
                extensionNotFound(InsurancePlanStructureDefinitions.IS_PRE_CERTIFICATION_REQUIRED));
    extensionForSystem(
            insurancePlan().extension(),
            InsurancePlanStructureDefinitions.EXCLUDE_PRE_EXISTING_CONDITION)
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(
                    e, GroupInsurancePlan.EXCLUDE_PRE_EXISTING_CONDITION))
        .ifPresentOrElse(
            fields::add,
            () ->
                extensionNotFound(
                    InsurancePlanStructureDefinitions.EXCLUDE_PRE_EXISTING_CONDITION));
    extensionForSystem(
            insurancePlan().extension(), InsurancePlanStructureDefinitions.BENEFITS_ASSIGNABLE)
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(e, GroupInsurancePlan.BENEFITS_ASSIGNABLE_))
        .ifPresentOrElse(
            fields::add,
            () -> extensionNotFound(InsurancePlanStructureDefinitions.BENEFITS_ASSIGNABLE));
    fields.add(typeOfPlan(insurancePlan().plan()));
    extensionForSystem(
            insurancePlan().extension(),
            InsurancePlanStructureDefinitions.AMBULATORY_CARE_CERTIFICATION)
        .map(
            e ->
                extensionToBooleanWriteableFilemanValue(
                    e, GroupInsurancePlan.AMBULATORY_CARE_CERTIFICATION))
        .ifPresentOrElse(
            fields::add,
            () ->
                extensionNotFound(InsurancePlanStructureDefinitions.AMBULATORY_CARE_CERTIFICATION));
    fields.add(electronicPlanType(insurancePlan().type()));
    extensionForSystem(
            insurancePlan().extension(), InsurancePlanStructureDefinitions.PLAN_STANDARD_FTF)
        .map(this::planStandardFtf)
        .ifPresentOrElse(
            fields::addAll,
            () -> extensionNotFound(InsurancePlanStructureDefinitions.PLAN_STANDARD_FTF));
    fields.add(groupName(insurancePlan().name()));
    fields.add(groupNumber(insurancePlan().identifier()));
    fields.add(planId(insurancePlan().identifier()));
    planCategory(insurancePlan().type()).ifPresent(fields::add);
    bankingIdentificationNumber(insurancePlan().identifier()).ifPresent(fields::add);
    processorControlNumber(insurancePlan().identifier()).ifPresent(fields::add);
    return fields;
  }

  WriteableFilemanValue typeOfPlan(List<InsurancePlan.Plan> plan) {
    return extractFromListOrDie(
        "plan",
        plan,
        c ->
            codeableconceptHasCodingSystem(
                c.type(), InsurancePlanStructureDefinitions.TYPE_OF_PLAN),
        c -> c.type().coding().stream().findFirst().map(Coding::display).orElse(null),
        GroupInsurancePlan.TYPE_OF_PLAN,
        InsurancePlanStructureDefinitions.TYPE_OF_PLAN + " system type not found");
  }
}
