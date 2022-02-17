package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toHumanDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.vistafhirquery.service.controller.ContainedResourceWriter;
import gov.va.api.health.vistafhirquery.service.controller.ContainedResourceWriter.ContainableResource;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InsuranceBufferToR4CoverageTransformer {
  public static final List<String> MAPPED_VISTA_FIELDS =
      List.of(
          InsuranceVerificationProcessor.BANKING_IDENTIFICATION_NUMBER,
          InsuranceVerificationProcessor.BILLING_PHONE_NUMBER,
          InsuranceVerificationProcessor.CITY,
          InsuranceVerificationProcessor.EFFECTIVE_DATE,
          InsuranceVerificationProcessor.EXPIRATION_DATE,
          InsuranceVerificationProcessor.GROUP_NAME,
          InsuranceVerificationProcessor.GROUP_NUMBER,
          InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1,
          InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME,
          InsuranceVerificationProcessor.INSUREDS_DOB,
          InsuranceVerificationProcessor.NAME_OF_INSURED,
          InsuranceVerificationProcessor.PATIENT_ID,
          InsuranceVerificationProcessor.PATIENT_NAME,
          InsuranceVerificationProcessor.PHONE_NUMBER,
          InsuranceVerificationProcessor.PRECERTIFICATION_PHONE_NUMBER,
          InsuranceVerificationProcessor.PROCESSOR_CONTROL_NUMBER_PCN,
          InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA,
          InsuranceVerificationProcessor.STATE,
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_1,
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_2,
          InsuranceVerificationProcessor.STREET_ADDRESS_LINE_3,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_CITY,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_COUNTRY,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_1,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_LINE_2,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_STATE,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_SUBDIVISION,
          InsuranceVerificationProcessor.SUBSCRIBER_ADDRESS_ZIP,
          InsuranceVerificationProcessor.SUBSCRIBER_ID,
          InsuranceVerificationProcessor.SUBSCRIBER_PHONE,
          InsuranceVerificationProcessor.TYPE_OF_PLAN,
          InsuranceVerificationProcessor.WHOSE_INSURANCE,
          InsuranceVerificationProcessor.ZIP_CODE);

  @NonNull String patientIcn;

  @NonNull String site;

  /** Assumes UTC if zoneId is not provided. */
  @Builder.Default ZoneId vistaZoneId = ZoneOffset.UTC;

  @NonNull LhsLighthouseRpcGatewayResponse.Results results;

  private Reference beneficiary(@NonNull String patientIcn, Optional<String> maybePatientId) {
    var ref = toReference(Patient.class.getSimpleName(), patientIcn, null);
    if (ref == null) {
      throw new IllegalStateException(
          "Reference could not be created using a non-null parameter: " + patientIcn);
    }
    maybePatientId.ifPresent(
        patientId ->
            ref.identifier(
                Identifier.builder()
                    .type(
                        CodeableConcept.builder()
                            .coding(
                                Coding.builder()
                                    .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                                    .code("MB")
                                    .display("Member Number")
                                    .build()
                                    .asList())
                            .build())
                    .value(patientId)
                    .build()));
    return ref;
  }

  private ContainedResourceWriter<Coverage> contained(FilemanEntry entry) {
    return ContainedResourceWriter.of(
        List.of(
            ContainableResource.<Coverage, InsurancePlan>builder()
                .containedResource(toInsurancePlan(entry))
                .applyReferenceId((c, id) -> c.coverageClass(coverageClass(id)))
                .build(),
            ContainableResource.<Coverage, Organization>builder()
                .containedResource(toOrganization(entry))
                .applyReferenceId(
                    (o, id) -> o.payor(Reference.builder().reference(id).build().asList()))
                .build()));
  }

  private List<Coverage.CoverageClass> coverageClass(String referenceId) {
    return Coverage.CoverageClass.builder()
        .type(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/coverage-class")
                            .code("group")
                            .build()))
                .build())
        .value(referenceId)
        .build()
        .asList();
  }

  private Period period(FilemanEntry entry) {
    Period period = Period.builder().build();
    entry
        .internal(InsuranceVerificationProcessor.EFFECTIVE_DATE, this::toFilemanDate)
        .ifPresent(period::start);
    entry
        .internal(InsuranceVerificationProcessor.EXPIRATION_DATE, this::toFilemanDate)
        .ifPresent(period::end);
    if (allBlank(period.start(), period.end())) {
      return null;
    }
    return period;
  }

  private CodeableConcept relationship(String ptRelationshipHipaa) {
    if (isBlank(ptRelationshipHipaa)) {
      return null;
    }
    return SubscriberToBeneficiaryRelationship.forCode(ptRelationshipHipaa)
        .map(
            relationship ->
                CodeableConcept.builder().coding(relationship.asCoding().asList()).build())
        .orElse(null);
  }

  private Coverage toCoverage(FilemanEntry entry) {
    if (entry == null || isBlank(entry.fields())) {
      return null;
    }
    var coverage =
        Coverage.builder()
            .id(
                PatientTypeCoordinates.builder()
                    .icn(patientIcn())
                    .site(site())
                    .file(entry.file())
                    .ien(entry.ien())
                    .build()
                    .toString())
            .period(period(entry))
            .meta(Meta.builder().source(site()).build())
            .status(Status.draft)
            .type(
                entry
                    .internal(InsuranceVerificationProcessor.INQ_SERVICE_TYPE_CODE_1)
                    .map(this::type)
                    .orElse(null))
            .subscriberId(entry.internal(InsuranceVerificationProcessor.SUBSCRIBER_ID).orElse(null))
            .beneficiary(
                beneficiary(
                    patientIcn(), entry.internal(InsuranceVerificationProcessor.PATIENT_ID)))
            .relationship(
                entry
                    .internal(InsuranceVerificationProcessor.PT_RELATIONSHIP_HIPAA)
                    .map(this::relationship)
                    .orElse(null))
            .build();
    contained(entry).addContainedResources(coverage);
    return coverage;
  }

  public Stream<Coverage> toFhir() {
    return Safe.stream(results.results()).map(this::toCoverage).filter(Objects::nonNull);
  }

  private String toFilemanDate(String filemanDate) {
    return toHumanDateTime(filemanDate, vistaZoneId())
        .map(t -> t.atZone(ZoneOffset.UTC))
        .map(t -> t.format(DateTimeFormatter.ISO_DATE_TIME))
        .orElse(null);
  }

  private InsurancePlan toInsurancePlan(FilemanEntry entry) {
    var ip =
        InsurancePlan.builder()
            .identifier(
                entry
                    .internal(InsuranceVerificationProcessor.GROUP_NUMBER)
                    .map(
                        num ->
                            Identifier.builder()
                                .system(InsuranceBufferStructureDefinitions.GROUP_NUMBER)
                                .value(num)
                                .build()
                                .asList())
                    .orElse(null))
            .name(entry.internal(InsuranceVerificationProcessor.GROUP_NAME).orElse(null))
            .build();
    if (InsurancePlan.builder().build().equals(ip)) {
      return null;
    }
    return ip;
  }

  private Organization toOrganization(FilemanEntry entry) {
    var org =
        Organization.builder()
            .active(true)
            .name(
                entry.internal(InsuranceVerificationProcessor.INSURANCE_COMPANY_NAME).orElse(null))
            .build();
    if (Organization.builder().active(true).build().equals(org)) {
      return null;
    }
    return org;
  }

  private CodeableConcept type(String inqServiceTypeCode) {
    if (isBlank(inqServiceTypeCode)) {
      return null;
    }
    return CodeableConcept.builder()
        .coding(
            Coding.builder()
                .system(InsuranceBufferStructureDefinitions.INQ_SERVICE_TYPE_CODE)
                .code(inqServiceTypeCode)
                .build()
                .asList())
        .build();
  }
}
