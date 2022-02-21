package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isStringLengthInRangeInclusively;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.tryParseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.COVERAGE_CLASS_CODE_SYSTEM;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.OPTIONAL;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.ContainedResourceReader;
import gov.va.api.health.vistafhirquery.service.controller.FilemanFactoryRegistry;
import gov.va.api.health.vistafhirquery.service.controller.FilemanIndexRegistry;
import gov.va.api.health.vistafhirquery.service.controller.IdentifierReader;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.EndDateOccursBeforeStartDate;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidStringLengthInclusively;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.BooleanExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionProcessor;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.lighthouse.charon.models.FilemanDate;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageToInsuranceBufferTransformer {
  static final Map<Boolean, String> YES_NO = Map.of(true, "YES", false, "NO");

  FilemanFactoryRegistry factoryRegistry;

  FilemanIndexRegistry indexRegistry;

  DateTimeFormatter vistaDateFormatter;

  ContainedResourceReader containedResourceReader;

  @NonNull Coverage coverage;

  ExtensionProcessor insurancePlanExtensionProcessor;

  @Builder
  R4CoverageToInsuranceBufferTransformer(@NonNull Coverage coverage, ZoneId timezone) {
    this.coverage = coverage;
    this.containedResourceReader = new ContainedResourceReader(coverage);
    this.factoryRegistry = FilemanFactoryRegistry.create();
    this.indexRegistry = FilemanIndexRegistry.create();
    this.vistaDateFormatter =
        DateTimeFormatter.ofPattern("MMddyyy")
            .withZone(timezone == null ? ZoneId.of("UTC") : timezone);
    this.insurancePlanExtensionProcessor =
        R4ExtensionProcessor.of(".extension[]", insurancePlanExtensionHandlers());
  }

  WriteableFilemanValue dateEntered() {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.DATE_ENTERED,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            today())
        .get();
  }

  List<WriteableFilemanValue> effectiveAndExpirationDate(Period period) {
    if (isBlank(period)) {
      throw MissingRequiredField.builder().jsonPath(".period").build();
    }
    List<WriteableFilemanValue> dates = new ArrayList<>(2);
    var startInstant = tryParseDateTime(period.start());
    startInstant
        .map(this::parseFilemanDateIgnoringTime)
        .flatMap(
            start ->
                factoryRegistry()
                    .get(InsuranceVerificationProcessor.FILE_NUMBER)
                    .forString(
                        InsuranceVerificationProcessor.EFFECTIVE_DATE,
                        indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
                        start))
        .ifPresentOrElse(
            dates::add,
            () -> {
              throw UnexpectedValueForField.builder()
                  .jsonPath(".period.start")
                  .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                  .valueReceived(period.start())
                  .build();
            });
    var endDateTime =
        tryParseDateTime(period.end())
            .orElseThrow(
                () ->
                    UnexpectedValueForField.builder()
                        .jsonPath(".period.end")
                        .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                        .valueReceived(period.end())
                        .build());
    if (!endDateTime.isAfter(startInstant.get())) {
      throw EndDateOccursBeforeStartDate.builder().jsonPath(".period").build();
    }
    var endDate = parseFilemanDateIgnoringTime(endDateTime);
    factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.EXPIRATION_DATE,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            endDate)
        .ifPresentOrElse(
            dates::add,
            () -> {
              throw UnexpectedValueForField.builder()
                  .jsonPath(".period.end")
                  .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                  .valueReceived(period.end())
                  .build();
            });
    return dates;
  }

  WriteableFilemanValue groupName(String name) {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.GROUP_NAME,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            name)
        .orElse(null);
  }

  WriteableFilemanValue groupNumber(List<Identifier> identifiers) {
    List<Identifier> groupNumbers =
        Safe.stream(identifiers)
            .filter(i -> InsuranceBufferStructureDefinitions.GROUP_NUMBER.equals(i.system()))
            .toList();
    if (groupNumbers.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .exactExpectedCount(1)
          .receivedCount(groupNumbers.size())
          .jsonPath(".contained[].identifiers[]")
          .build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.GROUP_NUMBER,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            groupNumbers.get(0).value())
        .orElseThrow(
            () ->
                MissingRequiredField.builder()
                    .jsonPath(".contained[].identifiers[].value")
                    .build());
  }

  // TODO: add system urn checking for protection https://vajira.max.gov/browse/API-12859
  WriteableFilemanValue inqServiceTypeCode1(CodeableConcept type) {
    if (isBlank(type) || isBlank(type.coding())) {
      throw MissingRequiredField.builder().jsonPath(".type.coding[]").build();
    }
    if (type.coding().size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".type.coding[]")
          .exactExpectedCount(1)
          .receivedCount(type.coding().size())
          .build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forCoding(
            InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            type.coding().get(0))
        .orElseThrow(() -> MissingRequiredField.builder().jsonPath(".type.coding[].code").build());
  }

  // TODO: get insurance company name from contained resource
  // https://vajira.max.gov/browse/API-13036
  WriteableFilemanValue insuranceCompanyName(String maybeName) {
    if (isBlank(maybeName)) {
      throw MissingRequiredField.builder().jsonPath(".name").build();
    }
    if (!isStringLengthInRangeInclusively(3, 30, maybeName)) {
      throw InvalidStringLengthInclusively.builder()
          .jsonPath(".name")
          .inclusiveMinimum(3)
          .inclusiveMaximum(30)
          .received(maybeName.length())
          .build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            maybeName)
        .get();
  }

  List<WriteableFilemanValue> insurancePlan() {
    if (isBlank(coverage().coverageClass())) {
      throw MissingRequiredField.builder().jsonPath(".coverageClass[]").build();
    }
    if (isBlank(coverage().contained())) {
      throw MissingRequiredField.builder().jsonPath(".contained[]").build();
    }
    var filteredCoverageTypes =
        coverage().coverageClass().stream().filter(this::isGroupPlan).collect(toList());
    if (filteredCoverageTypes.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".class")
          .identifyingFieldValue(".type[].coding[].code")
          .identifyingFieldValue("group")
          .exactExpectedCount(1)
          .receivedCount(filteredCoverageTypes.size())
          .build();
    }
    String referenceId = filteredCoverageTypes.get(0).value();
    InsurancePlan containedInsurancePlan =
        containedResourceReader.find(InsurancePlan.class, referenceId);
    var reader =
        IdentifierReader.builder()
            .readableIdentifierDefinitions(insurancePlanIdentifierRecords())
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .indexRegistry(indexRegistry())
            .build();
    try {
      return Stream.concat(
              Stream.of(
                  groupName(containedInsurancePlan.name()),
                  typeOfPlan(containedInsurancePlan.plan())),
              Stream.of(
                      insurancePlanExtensionProcessor().process(containedInsurancePlan.extension()),
                      reader.process(containedInsurancePlan.identifier()))
                  .flatMap(Collection::stream))
          .filter(Objects::nonNull)
          .toList();
    } catch (BadRequestPayload e) {
      throw RequestPayloadExceptions.InvalidContainedResource.builder()
          .resourceType(InsurancePlan.class)
          .id(referenceId)
          .cause(e)
          .build();
    }
  }

  private List<ExtensionHandler> insurancePlanExtensionHandlers() {
    return List.of(
        BooleanExtensionHandler.forDefiningUrl(
                InsuranceBufferStructureDefinitions.UTILIZATION_REVIEW_REQUIRED)
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .fieldNumber(InsuranceVerificationProcessor.UTILIZATION_REVIEW_REQUIRED)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                InsuranceBufferStructureDefinitions.PRECERTIFICATION_REQUIRED)
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .fieldNumber(InsuranceVerificationProcessor.PRECERTIFICATION_REQUIRED)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                InsuranceBufferStructureDefinitions.AMBULATORY_CARE_CERTIFICATION)
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .fieldNumber(InsuranceVerificationProcessor.AMBULATORY_CARE_CERTIFICATION)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                InsuranceBufferStructureDefinitions.EXCLUDE_PREEXISTING_CONDITION)
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .fieldNumber(InsuranceVerificationProcessor.EXCLUDE_PREEXISTING_CONDITION)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build(),
        BooleanExtensionHandler.forDefiningUrl(
                InsuranceBufferStructureDefinitions.BENEFITS_ASSIGNABLE)
            .filemanFactory(factoryRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
            .fieldNumber(InsuranceVerificationProcessor.BENEFITS_ASSIGNABLE)
            .index(1)
            .required(OPTIONAL)
            .booleanStringMapping(YES_NO)
            .build());
  }

  private List<IdentifierReader.ReadableIdentifierDefinition> insurancePlanIdentifierRecords() {
    return List.of(
        IdentifierReader.ReadableIdentifierDefinition.builder()
            .field(InsuranceVerificationProcessor.GROUP_NUMBER)
            .system(InsuranceBufferStructureDefinitions.GROUP_NUMBER)
            .isRequired(true)
            .build(),
        IdentifierReader.ReadableIdentifierDefinition.builder()
            .field(InsuranceVerificationProcessor.BANKING_IDENTIFICATION_NUMBER)
            .system(InsuranceBufferStructureDefinitions.BANKING_IDENTIFICATION_NUMBER)
            .isRequired(false)
            .build(),
        IdentifierReader.ReadableIdentifierDefinition.builder()
            .field(InsuranceVerificationProcessor.PROCESSOR_CONTROL_NUMBER_PCN)
            .system(InsuranceBufferStructureDefinitions.PROCESSOR_CONTROL_NUMBER_PCN)
            .isRequired(false)
            .build());
  }

  private boolean isGroupPlan(Coverage.CoverageClass c) {
    if (c.type() == null) {
      return false;
    }
    return c.type().coding().stream()
        .anyMatch(
            coding ->
                COVERAGE_CLASS_CODE_SYSTEM.equals(coding.system())
                    && "group".equals(coding.code()));
  }

  Optional<WriteableFilemanValue> nameOfInsured(Reference subscriber) {
    if (isBlank(subscriber)) {
      return Optional.empty();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.NAME_OF_INSURED,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            subscriber.display());
  }

  WriteableFilemanValue overrideFreshnessFlag() {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.OVERRIDE_FRESHNESS_FLAG,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            "0")
        .get();
  }

  private String parseFilemanDateIgnoringTime(Instant instant) {
    return FilemanDate.from(instant.truncatedTo(ChronoUnit.DAYS))
        .formatAsDateTime(ZoneId.of("UTC"));
  }

  WriteableFilemanValue patientId(Reference beneficiary) {
    if (isBlank(beneficiary) || isBlank(beneficiary.identifier())) {
      throw MissingRequiredField.builder().jsonPath(".beneficiary.identifier").build();
    }
    var isMemberId =
        Optional.ofNullable(beneficiary.identifier().type()).map(CodeableConcept::coding).stream()
            .flatMap(Collection::stream)
            .anyMatch(coding -> "MB".equals(coding.code()));
    if (!isMemberId) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".beneficiary.identifier")
          .identifyingFieldJsonPath(".type.coding[].code")
          .identifyingFieldValue("MB")
          .exactExpectedCount(1)
          .receivedCount(0)
          .build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forIdentifier(
            InsuranceVerificationProcessor.PATIENT_ID,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            beneficiary.identifier())
        .orElseThrow(
            () -> MissingRequiredField.builder().jsonPath(".beneficiary.identifier.value").build());
  }

  WriteableFilemanValue patientRelationshipHipaa(CodeableConcept relationship) {
    if (isBlank(relationship) || isBlank(relationship.coding())) {
      throw MissingRequiredField.builder().jsonPath(".relationship.coding[]").build();
    }
    var relationships =
        relationship.coding().stream()
            .filter(c -> SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM.equals(c.system()))
            .toList();
    if (relationships.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".relationship")
          .identifyingFieldJsonPath(".coding[].system")
          .identifyingFieldValue(SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM)
          .exactExpectedCount(1)
          .receivedCount(relationships.size())
          .build();
    }
    var relationshipCode = relationships.get(0);
    return SubscriberToBeneficiaryRelationship.fromCoding(relationshipCode)
        .map(
            factoryRegistry()
                .get(InsuranceVerificationProcessor.FILE_NUMBER)
                .toString(
                    InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA,
                    index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER)),
                    SubscriberToBeneficiaryRelationship::display))
        .orElseThrow(
            () ->
                UnexpectedValueForField.builder()
                    .jsonPath(".relationship.coding[]")
                    .valueSet(SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM)
                    .valueReceived(relationshipCode)
                    .build());
  }

  WriteableFilemanValue serviceDate() {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.SERVICE_DATE,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            today())
        .get();
  }

  WriteableFilemanValue sourceOfInformation() {
    return WriteableFilemanValue.builder()
        .file("355.12")
        .index(indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER))
        .field("ien")
        .value("22")
        .build();
  }

  WriteableFilemanValue status() {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.STATUS,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            "E")
        .get();
  }

  WriteableFilemanValue subscriberId(String subscriberId) {
    if (isBlank(subscriberId)) {
      throw MissingRequiredField.builder().jsonPath(".subscriberId").build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.SUBSCRIBER_ID,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            subscriberId)
        .get();
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceBuffer() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    fields.add(status());
    fields.add(overrideFreshnessFlag());
    fields.add(dateEntered());
    fields.add(sourceOfInformation());
    fields.add(serviceDate());
    fields.add(whoseInsurance());
    fields.add(insuranceCompanyName("Placeholder InsCo Name"));
    fields.add(patientId(coverage().beneficiary()));
    fields.add(patientRelationshipHipaa(coverage().relationship()));
    fields.add(subscriberId(coverage().subscriberId()));
    nameOfInsured(coverage().subscriber()).ifPresent(fields::add);
    fields.add(inqServiceTypeCode1(coverage().type()));
    fields.addAll(effectiveAndExpirationDate(coverage().period()));
    fields.addAll(insurancePlan());
    return fields;
  }

  private String today() {
    return vistaDateFormatter().format(Instant.now());
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
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.TYPE_OF_PLAN,
            indexRegistry().get(InsuranceVerificationProcessor.TYPE_OF_PLAN),
            type.coding().get(0).display())
        .orElse(null);
  }

  // TODO: get whose insurance from contained resource
  // https://vajira.max.gov/browse/API-13036
  WriteableFilemanValue whoseInsurance() {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.WHOSE_INSURANCE,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            "s")
        .get();
  }
}
