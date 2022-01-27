package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
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
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageToInsuranceBufferTransformer {
  FilemanFactoryRegistry factoryRegistry;

  FilemanIndexRegistry indexRegistry;

  DateTimeFormatter vistaDateFormatter;

  @NonNull Coverage coverage;

  @Builder
  R4CoverageToInsuranceBufferTransformer(@NonNull Coverage coverage, ZoneId timezone) {
    this.coverage = coverage;
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

  WriteableFilemanValue nameOfInsured(Reference subscriber) {
    if (isBlank(subscriber)) {
      throw MissingRequiredField.builder().jsonPath(".subscriber").build();
    }
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.NAME_OF_INSURED,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            subscriber.display())
        .orElseThrow(() -> MissingRequiredField.builder().jsonPath(".subscriber.display").build());
  }

  WriteableFilemanValue overrideFreshnessFlag() {
    return factoryRegistry()
        .get(InsuranceVerificationProcessor.FILE_NUMBER)
        .forString(
            InsuranceVerificationProcessor.OVERRIDE_FRESHNESS_FLAG,
            indexRegistry().get(InsuranceVerificationProcessor.FILE_NUMBER),
            "1")
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
    fields.add(nameOfInsured(coverage.subscriber()));
    fields.add(inqServiceTypeCode1(coverage.type()));
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
