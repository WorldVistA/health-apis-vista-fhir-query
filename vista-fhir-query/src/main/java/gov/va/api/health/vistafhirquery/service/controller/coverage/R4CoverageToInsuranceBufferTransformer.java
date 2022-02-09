package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.COVERAGE_CLASS_CODE_SYSTEM;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.ContainedResourceReader;
import gov.va.api.health.vistafhirquery.service.controller.FilemanFactoryRegistry;
import gov.va.api.health.vistafhirquery.service.controller.FilemanIndexRegistry;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageToInsuranceBufferTransformer {
  FilemanFactoryRegistry factoryRegistry;

  FilemanIndexRegistry indexRegistry;

  DateTimeFormatter vistaDateFormatter;

  ContainedResourceReader containedResourceReader;

  @NonNull Coverage coverage;

  @Builder
  R4CoverageToInsuranceBufferTransformer(@NonNull Coverage coverage, ZoneId timezone) {
    this.coverage = coverage;
    this.containedResourceReader = new ContainedResourceReader(coverage);
    this.factoryRegistry = FilemanFactoryRegistry.create();
    this.indexRegistry = FilemanIndexRegistry.create();
    this.vistaDateFormatter =
        DateTimeFormatter.ofPattern("MMddyyy")
            .withZone(timezone == null ? ZoneId.of("UTC") : timezone);
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
        identifiers.stream()
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
  WriteableFilemanValue insuranceCompanyName() {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            "Placeholder Insurance Company Name")
        .get();
  }

  List<WriteableFilemanValue> insurancePlan() {
    if (isBlank(coverage.coverageClass())) {
      throw MissingRequiredField.builder().jsonPath(".coverageClass[]").build();
    }
    if (isBlank(coverage.contained())) {
      throw MissingRequiredField.builder().jsonPath(".contained[]").build();
    }
    var filteredCoverageTypes =
        coverage.coverageClass().stream().filter(this::isGroupPlan).collect(toList());
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
    return Stream.of(
            groupName(containedInsurancePlan.name()),
            groupNumber(containedInsurancePlan.identifier()))
        .filter(Objects::nonNull)
        .toList();
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
    fields.add(insuranceCompanyName());
    fields.add(patientId(coverage.beneficiary()));
    fields.add(patientRelationshipHipaa(coverage.relationship()));
    fields.add(subscriberId(coverage.subscriberId()));
    nameOfInsured(coverage.subscriber()).ifPresent(fields::add);
    fields.add(inqServiceTypeCode1(coverage.type()));
    fields.addAll(insurancePlan());
    return fields;
  }

  private String today() {
    return vistaDateFormatter.format(Instant.now());
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
