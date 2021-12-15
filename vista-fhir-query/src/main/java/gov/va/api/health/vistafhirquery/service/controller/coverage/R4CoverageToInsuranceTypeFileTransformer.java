package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.recordCoordinatesForReference;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.tryFormatDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.tryParseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.COVERAGE_CLASS_CODE_SYSTEM;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.CoverageClass;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.vistafhirquery.service.controller.FilemanFactoryRegistry;
import gov.va.api.health.vistafhirquery.service.controller.FilemanIndexRegistry;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.EndDateOccursBeforeStartDate;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidConditionalField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidReferenceId;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.BooleanExtensionHandler;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.R4ExtensionProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageToInsuranceTypeFileTransformer {
  FilemanFactoryRegistry factoryRegistry;

  FilemanIndexRegistry indexRegistry;

  @NonNull Coverage coverage;

  DateTimeFormatter vistaDateFormatter;

  @Builder
  R4CoverageToInsuranceTypeFileTransformer(@NonNull Coverage coverage, ZoneId timezone) {
    this.coverage = coverage;
    this.vistaDateFormatter =
        DateTimeFormatter.ofPattern("MM-dd-yyy")
            .withZone(timezone == null ? ZoneId.of("UTC") : timezone);
    this.factoryRegistry = FilemanFactoryRegistry.create();
    this.indexRegistry = FilemanIndexRegistry.create();
  }

  @SuppressWarnings("UnnecessaryParentheses")
  WriteableFilemanValue coordinationOfBenefits(Integer order) {
    if (isBlank(order)) {
      throw MissingRequiredField.builder().jsonPath(".order").build();
    }
    var priority =
        switch (order) {
          case 1 -> "PRIMARY";
          case 2 -> "SECONDARY";
          case 3 -> "TERTIARY";
          default -> throw UnexpectedValueForField.builder()
              .jsonPath(".order")
              .supportedValues(List.of(1, 2, 3))
              .valueReceived(order)
              .build();
        };
    return factoryRegistry()
        .get(InsuranceType.FILE_NUMBER)
        .forString(
            InsuranceType.COORDINATION_OF_BENEFITS,
            indexRegistry().get(InsuranceType.FILE_NUMBER),
            priority)
        .get();
  }

  private R4ExtensionProcessor extensionProcessor() {
    return R4ExtensionProcessor.of(
        ".extension[]",
        BooleanExtensionHandler.forDefiningUrl(
                CoverageStructureDefinitions.STOP_POLICY_FROM_BILLING)
            .required(Required.REQUIRED)
            .fieldNumber(InsuranceType.STOP_POLICY_FROM_BILLING)
            .index(indexRegistry().get(InsuranceType.FILE_NUMBER))
            .filemanFactory(factoryRegistry().get(InsuranceType.FILE_NUMBER))
            .booleanStringMapping(Map.of(true, "YES", false, "NO"))
            .build());
  }

  WriteableFilemanValue groupPlan(List<CoverageClass> coverageTypes) {
    if (isBlank(coverageTypes)) {
      throw MissingRequiredField.builder().jsonPath(".class").build();
    }
    var filteredCoverageTypes =
        coverageTypes.stream()
            .filter(
                c -> {
                  if (c.type() == null) {
                    return false;
                  }
                  return c.type().coding().stream()
                      .anyMatch(
                          coding ->
                              COVERAGE_CLASS_CODE_SYSTEM.equals(coding.system())
                                  && "group".equals(coding.code()));
                })
            .collect(toList());
    if (filteredCoverageTypes.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".class")
          .identifyingFieldValue(".type[].coding[].code")
          .identifyingFieldValue("group")
          .expectedCount(1)
          .receivedCount(filteredCoverageTypes.size())
          .build();
    }
    // Group slice cardinality is 0..1
    return recordCoordinatesForReference(
            Reference.builder().reference(filteredCoverageTypes.get(0).value()).build())
        .map(
            factoryRegistry()
                .get(InsuranceType.FILE_NUMBER)
                .recordCoordinatesToPointer(
                    GroupInsurancePlan.FILE_NUMBER,
                    index(indexRegistry().get(InsuranceType.FILE_NUMBER))))
        .orElseThrow(
            () ->
                InvalidReferenceId.builder()
                    .jsonPath(".class[].value")
                    .referenceType("InsurancePlan")
                    .build());
  }

  WriteableFilemanValue insuranceType(List<Reference> payors) {
    if (isBlank(payors)) {
      throw MissingRequiredField.builder().jsonPath(".payor").build();
    }
    if (payors.size() != 1) {
      throw UnexpectedNumberOfValues.builder()
          .jsonPath(".payor")
          .expectedCount(1)
          .receivedCount(payors.size())
          .build();
    }
    return recordCoordinatesForReference(payors.get(0))
        .map(
            factoryRegistry()
                .get(InsuranceType.FILE_NUMBER)
                .recordCoordinatesToPointer(
                    InsuranceCompany.FILE_NUMBER,
                    index(indexRegistry().get(InsuranceType.FILE_NUMBER))))
        .orElseThrow(
            () ->
                InvalidReferenceId.builder()
                    .jsonPath(".payor[].reference")
                    .referenceType("Organization")
                    .build());
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
          .expectedCount(1)
          .receivedCount(0)
          .build();
    }
    return factoryRegistry()
        .get(InsuranceType.FILE_NUMBER)
        .forIdentifier(
            InsuranceType.PATIENT_ID,
            indexRegistry().get(InsuranceType.FILE_NUMBER),
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
          .expectedCount(1)
          .receivedCount(relationships.size())
          .build();
    }
    var relationshipCode = relationships.get(0);
    return SubscriberToBeneficiaryRelationship.fromCoding(relationshipCode)
        .map(
            factoryRegistry()
                .get(InsuranceType.FILE_NUMBER)
                .toString(
                    InsuranceType.PT_RELATIONSHIP_HIPAA,
                    index(indexRegistry().get(InsuranceType.FILE_NUMBER)),
                    SubscriberToBeneficiaryRelationship::display))
        .orElseThrow(
            () ->
                UnexpectedValueForField.builder()
                    .jsonPath(".relationship.coding[]")
                    .valueSet(SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM)
                    .valueReceived(relationshipCode)
                    .build());
  }

  List<WriteableFilemanValue> policyStartAndEndDates(Period period) {
    if (isBlank(period) || isBlank(period.start())) {
      throw MissingRequiredField.builder().jsonPath(".period.start").build();
    }
    List<WriteableFilemanValue> dates = new ArrayList<>(2);
    var startInstant =
        tryParseDateTime(period.start())
            .orElseThrow(
                () ->
                    RequestPayloadExceptions.UnexpectedValueForField.builder()
                        .jsonPath(".period.start")
                        .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                        .valueReceived(period.start())
                        .build());
    var effectiveDate =
        tryFormatDateTime(startInstant, vistaDateFormatter)
            .flatMap(
                start ->
                    factoryRegistry()
                        .get(InsuranceType.FILE_NUMBER)
                        .forString(
                            InsuranceType.EFFECTIVE_DATE_OF_POLICY,
                            indexRegistry().get(InsuranceType.FILE_NUMBER),
                            start))
            .orElseThrow(
                () ->
                    RequestPayloadExceptions.UnexpectedValueForField.builder()
                        .jsonPath(".period.start")
                        .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                        .valueReceived(period.start())
                        .build());
    dates.add(effectiveDate);
    if (isBlank(period.end())) {
      return dates;
    }
    var endInstant =
        tryParseDateTime(period.end())
            .orElseThrow(
                () ->
                    RequestPayloadExceptions.UnexpectedValueForField.builder()
                        .jsonPath(".period.end")
                        .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                        .valueReceived(period.end())
                        .build());
    if (!endInstant.isAfter(startInstant)) {
      throw EndDateOccursBeforeStartDate.builder().jsonPath(".period").build();
    }
    /* When expiration date (conditionally required) is present,
     * any failure to process it should result in failure. */
    var expire =
        tryFormatDateTime(endInstant, vistaDateFormatter)
            .flatMap(
                end ->
                    factoryRegistry()
                        .get(InsuranceType.FILE_NUMBER)
                        .forString(
                            InsuranceType.INSURANCE_EXPIRATION_DATE,
                            indexRegistry().get(InsuranceType.FILE_NUMBER),
                            end))
            .orElseThrow(
                () ->
                    InvalidConditionalField.builder()
                        .jsonPath(".period.end")
                        .condition("was present but not a parsable date.")
                        .build());
    dates.add(expire);
    return dates;
  }

  WriteableFilemanValue subscriberId(String subscriberId) {
    if (isBlank(subscriberId)) {
      throw MissingRequiredField.builder().jsonPath(".subscriberId").build();
    }
    return factoryRegistry()
        .get(InsuranceType.FILE_NUMBER)
        .forString(
            InsuranceType.SUBSCRIBER_ID,
            indexRegistry().get(InsuranceType.FILE_NUMBER),
            subscriberId)
        .get();
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceTypeFile() {
    if (!Status.active.equals(coverage().status())) {
      throw UnexpectedValueForField.builder()
          .jsonPath(".status")
          .supportedValues(List.of(Status.active))
          .valueReceived(coverage().status())
          .build();
    }
    Set<WriteableFilemanValue> fields = new HashSet<>();
    Optional.ofNullable(coverage().id())
        .map(PatientTypeCoordinates::fromString)
        .map(
            factoryRegistry()
                .get(InsuranceType.FILE_NUMBER)
                .patientTypeCoordinatesToPointer(
                    InsuranceType.FILE_NUMBER,
                    index(indexRegistry().get(InsuranceType.FILE_NUMBER))))
        .ifPresent(fields::add);
    fields.addAll(extensionProcessor().process(coverage().extension()));
    fields.add(insuranceType(coverage().payor()));
    fields.add(groupPlan(coverage().coverageClass()));
    fields.add(coordinationOfBenefits(coverage().order()));
    fields.add(patientRelationshipHipaa(coverage().relationship()));
    fields.add(patientId(coverage().beneficiary()));
    Optional.ofNullable(coverage().dependent())
        .map(
            factoryRegistry()
                .get(InsuranceType.FILE_NUMBER)
                .toString(
                    InsuranceType.PHARMACY_PERSON_CODE,
                    index(indexRegistry().get(InsuranceType.FILE_NUMBER)),
                    identity()))
        .ifPresent(fields::add);
    fields.add(subscriberId(coverage().subscriberId()));
    fields.addAll(policyStartAndEndDates(coverage().period()));
    return fields;
  }
}
