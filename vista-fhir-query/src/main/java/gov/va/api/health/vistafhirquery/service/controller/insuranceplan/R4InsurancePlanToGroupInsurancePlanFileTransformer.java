package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.codeableconceptHasCodingSystem;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.identifierHasCodingSystem;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.recordCoordinatesForReference;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static java.util.function.Function.identity;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredListItem;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.BooleanExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionProcessor;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.QuantityExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4InsurancePlanToGroupInsurancePlanFileTransformer {
  static final Map<Boolean, String> YES_NO = Map.of(true, "YES", false, "NO");

  private static final WriteableFilemanValueFactory filemanFactory =
      WriteableFilemanValueFactory.forFile(GroupInsurancePlan.FILE_NUMBER);

  @NonNull InsurancePlan insurancePlan;

  ExtensionProcessor extensionProcessor =
      R4ExtensionProcessor.of(".extension[]", extensionHandlers());

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
        ".type[]",
        List.of(
            ".type[].coding[].system = " + InsurancePlanStructureDefinitions.ELECTRONIC_PLAN_TYPE),
        codeableConcepts,
        c ->
            codeableconceptHasCodingSystem(
                c, InsurancePlanStructureDefinitions.ELECTRONIC_PLAN_TYPE),
        this::extractCodeFromCoding,
        GroupInsurancePlan.ELECTRONIC_PLAN_TYPE);
  }

  private List<ExtensionHandler> extensionHandlers() {
    return Stream.of(extensionHandlersBoolean(), extensionHandlersQuantity())
        .flatMap(Collection::stream)
        .toList();
  }

  private Set<ExtensionHandler> extensionHandlersBoolean() {
    return Set.of(
        BooleanExtensionHandler.forDefiningUrl(
                InsurancePlanStructureDefinitions.IS_UTILIZATION_REVIEW_REQUIRED)
            .filemanFactory(filemanFactory)
            .fieldNumber(GroupInsurancePlan.IS_UTILIZATION_REVIEW_REQUIRED)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                InsurancePlanStructureDefinitions.IS_PRE_CERTIFICATION_REQUIRED)
            .filemanFactory(filemanFactory)
            .fieldNumber(GroupInsurancePlan.IS_PRE_CERTIFICATION_REQUIRED_)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                InsurancePlanStructureDefinitions.EXCLUDE_PRE_EXISTING_CONDITION)
            .filemanFactory(filemanFactory)
            .fieldNumber(GroupInsurancePlan.EXCLUDE_PRE_EXISTING_CONDITION)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                InsurancePlanStructureDefinitions.BENEFITS_ASSIGNABLE)
            .filemanFactory(filemanFactory)
            .fieldNumber(GroupInsurancePlan.BENEFITS_ASSIGNABLE_)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                InsurancePlanStructureDefinitions.AMBULATORY_CARE_CERTIFICATION)
            .filemanFactory(filemanFactory)
            .fieldNumber(GroupInsurancePlan.AMBULATORY_CARE_CERTIFICATION)
            .index(1)
            .required(REQUIRED)
            .booleanStringMapping(YES_NO)
            .build());
  }

  private Set<ExtensionHandler> extensionHandlersQuantity() {
    return Set.of(
        QuantityExtensionHandler.forDefiningUrl(InsurancePlanStructureDefinitions.PLAN_STANDARD_FTF)
            .required(REQUIRED)
            .valueFieldNumber(GroupInsurancePlan.PLAN_STANDARD_FTF_VALUE)
            .unitFieldNumber(GroupInsurancePlan.PLAN_STANDARD_FTF)
            .index(1)
            .filemanFactory(filemanFactory)
            .build());
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
      String jsonPath,
      List<String> qualifier,
      List<T> object,
      Predicate<? super T> predicate,
      Function<? super T, ? extends R> mapper,
      String field) {
    if (isBlank(object)) {
      throw MissingRequiredField.builder().jsonPath(jsonPath).build();
    }
    return extractFromList(object, predicate, mapper, field)
        .orElseThrow(
            () ->
                MissingRequiredListItem.builder().jsonPath(jsonPath).qualifiers(qualifier).build());
  }

  WriteableFilemanValue groupName(String name) {
    if (isBlank(name)) {
      throw MissingRequiredField.builder().jsonPath(".name").build();
    }
    return filemanFactory.forString(GroupInsurancePlan.GROUP_NAME, 1, name).get();
  }

  WriteableFilemanValue groupNumber(List<Identifier> identifiers) {
    return extractFromListOrDie(
        ".identifier[]",
        List.of(".identifier[].system = " + InsurancePlanStructureDefinitions.GROUP_NUMBER),
        identifiers,
        i -> identifierHasCodingSystem(i, InsurancePlanStructureDefinitions.GROUP_NUMBER),
        Identifier::value,
        GroupInsurancePlan.GROUP_NUMBER);
  }

  Optional<WriteableFilemanValue> insurancePlanIen(String id) {
    return Optional.ofNullable(id)
        .map(RecordCoordinates::fromString)
        .map(filemanFactory.recordCoordinatesToPointer(GroupInsurancePlan.FILE_NUMBER, index(1)));
  }

  WriteableFilemanValue ownedBy(Reference reference) {
    return recordCoordinatesForReference(reference)
        .map(filemanFactory.recordCoordinatesToPointer(InsuranceCompany.FILE_NUMBER, index(1)))
        .orElseThrow(() -> MissingRequiredField.builder().jsonPath(".ownedBy.reference").build());
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
        ".identifier[]",
        List.of(".identifier[].system = " + InsurancePlanStructureDefinitions.PLAN_ID),
        identifiers,
        i -> identifierHasCodingSystem(i, InsurancePlanStructureDefinitions.PLAN_ID),
        Identifier::value,
        GroupInsurancePlan.PLAN_ID);
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
    fields.add(ownedBy(insurancePlan().ownedBy()));
    fields.add(typeOfPlan(insurancePlan().plan()));
    fields.add(electronicPlanType(insurancePlan().type()));
    fields.add(groupName(insurancePlan().name()));
    fields.add(groupNumber(insurancePlan().identifier()));
    fields.add(planId(insurancePlan().identifier()));
    planCategory(insurancePlan().type()).ifPresent(fields::add);
    bankingIdentificationNumber(insurancePlan().identifier()).ifPresent(fields::add);
    processorControlNumber(insurancePlan().identifier()).ifPresent(fields::add);
    fields.addAll(extensionProcessor.process(insurancePlan().extension()));
    return fields;
  }

  WriteableFilemanValue typeOfPlan(List<InsurancePlan.Plan> plan) {
    if (isBlank(plan)) {
      throw MissingRequiredField.builder().jsonPath(".plan[]").build();
    }
    if (plan.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .receivedCount(plan.size())
          .exactExpectedCount(1)
          .jsonPath(".plan[]")
          .build();
    }
    var type = plan.get(0).type();
    if (isBlank(type)) {
      throw MissingRequiredField.builder().jsonPath(".plan[].type").build();
    }
    if (isBlank(type.coding())) {
      throw MissingRequiredField.builder().jsonPath(".plan[].type.coding[]").build();
    }
    if (type.coding().size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .receivedCount(type.coding().size())
          .exactExpectedCount(1)
          .jsonPath(".plan[].type.coding[]")
          .build();
    }
    return extractFromListOrDie(
        ".plan[].type.coding[]",
        List.of(".plan[].type.coding[].system = " + InsurancePlanStructureDefinitions.TYPE_OF_PLAN),
        type.coding(),
        c -> InsurancePlanStructureDefinitions.TYPE_OF_PLAN.equals(c.system()),
        Coding::display,
        GroupInsurancePlan.TYPE_OF_PLAN);
  }
}
