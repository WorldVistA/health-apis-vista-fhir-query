package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableCodeDefinition;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableCodeableConceptDefinition;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableDateDefinition;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableExtensionDefinition;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableIdentifierDefinition;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class InsuranceBufferDefinitions {
  private static final InsuranceBufferDefinitions INSTANCE = new InsuranceBufferDefinitions();

  public static InsuranceBufferDefinitions get() {
    return INSTANCE;
  }

  /** Banking Identification Number. */
  public MappableIdentifierDefinition bankingIdentificationNumber() {
    return MappableIdentifierDefinition.builder()
        .vistaField(InsuranceVerificationProcessor.BANKING_IDENTIFICATION_NUMBER)
        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.40801")
        .isRequired(false)
        .build();
  }

  /** Group Number. */
  public MappableIdentifierDefinition groupNumber() {
    return MappableIdentifierDefinition.builder()
        .vistaField(InsuranceVerificationProcessor.GROUP_NUMBER)
        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.908002")
        .isRequired(true)
        .build();
  }

  /** Inquiry Service Type Code. */
  public MappableCodeableConceptDefinition inqServiceTypeCode() {
    return MappableCodeableConceptDefinition.builder()
        .vistaField(InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1)
        .valueSet("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.8808001")
        .isRequired(true)
        .build();
  }

  /** Insureds Sex. */
  public MappableExtensionDefinition<MappableCodeDefinition<String, String>> insuredsSex() {
    return MappableExtensionDefinition.forValueDefinition(
            MappableCodeDefinition.<String, String>builder()
                .vistaField(InsuranceVerificationProcessor.INSUREDS_SEX)
                .fromCode(v -> Map.of("M", "M", "F", "F", "UNK", "EMPTY").get(v))
                .toCode(v -> isBlank(v) ? "UNK" : v)
                .isRequired(false)
                .build())
        .structureDefinition(
            "http://hl7.org/fhir/us/core/STU4/StructureDefinition-us-core-birthsex")
        .build();
  }

  /** Insureds SSN. */
  public MappableIdentifierDefinition insuredsSsn() {
    return MappableIdentifierDefinition.builder()
        .vistaField(InsuranceVerificationProcessor.INSUREDS_SSN)
        .system("http://hl7.org/fhir/sid/us-ssn")
        .isRequired(false)
        .build();
  }

  /** Processor Control Number. */
  public MappableIdentifierDefinition processorControlNumber() {
    return MappableIdentifierDefinition.builder()
        .vistaField(InsuranceVerificationProcessor.PROCESSOR_CONTROL_NUMBER_PCN)
        .system("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.408011")
        .isRequired(false)
        .build();
  }

  /** Will Reimburse for Care. */
  public MappableExtensionDefinition<MappableCodeableConceptDefinition> reimburse() {
    return MappableExtensionDefinition.forValueDefinition(
            MappableCodeableConceptDefinition.builder()
                .vistaField(InsuranceVerificationProcessor.REIMBURSE)
                .valueSet("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.208005")
                .isRequired(false)
                .build())
        .structureDefinition(
            "http://va.gov/fhir/StructureDefinition/organization-willReimburseForCare")
        .build();
  }

  /** Service Date. */
  public MappableExtensionDefinition<MappableDateDefinition> serviceDate() {
    return MappableExtensionDefinition.forValueDefinition(
            MappableDateDefinition.builder()
                .vistaField(InsuranceVerificationProcessor.SERVICE_DATE)
                .vistaDateFormatter(DateTimeFormatter.ofPattern("MMddyyyy"))
                .isRequired(false)
                .build())
        .structureDefinition("http://va.gov/fhir/StructureDefinition/coverage-serviceDate")
        .build();
  }

  /** Type of Plan. */
  public MappableCodeableConceptDefinition typeOfPlan() {
    return MappableCodeableConceptDefinition.builder()
        .vistaField(InsuranceVerificationProcessor.TYPE_OF_PLAN)
        .valueSet("urn:oid:2.16.840.1.113883.3.8901.3.1.3558033.408009")
        .isRequired(false)
        .build();
  }
}
