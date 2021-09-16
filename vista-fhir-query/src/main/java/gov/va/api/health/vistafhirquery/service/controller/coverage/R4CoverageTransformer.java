package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.patientCoordinateStringFrom;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.FilemanDate;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.UnexpectedVistaValue;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class R4CoverageTransformer {
  @NonNull Map.Entry<String, LhsLighthouseRpcGatewayResponse.Results> rpcResults;

  @NonNull String patientIcn;

  /** Assumes UTC if zoneId is not provided. */
  @Builder.Default ZoneId vistaZoneId = ZoneOffset.UTC;

  @SuppressWarnings("UnnecessaryParentheses")
  static boolean stopPolicyFromBillingToBoolean(String value) {
    return switch (value) {
      case "0" -> false;
      case "1" -> true;
      default -> throw new UnexpectedVistaValue(
          InsuranceType.STOP_POLICY_FROM_BILLING, value, "Expected 0 or 1");
    };
  }

  private Reference beneficiary(String patientIcn, Optional<String> memberId) {
    var reference = toReference("Patient", patientIcn, null);
    memberId.ifPresent(m -> reference.identifier(memberId(m)));
    return reference;
  }

  private List<Coverage.CoverageClass> classes(FilemanEntry entry) {
    return entry
        .internal(InsuranceType.GROUP_PLAN)
        .map(
            value ->
                RecordCoordinates.builder()
                    .site(site())
                    .file(GroupInsurancePlan.FILE_NUMBER)
                    .ien(value)
                    .build())
        .map(
            coords ->
                Coverage.CoverageClass.builder()
                    .value("InsurancePlan/" + coords.toString())
                    .type(coverageClass())
                    .build())
        .map(List::of)
        .orElse(null);
  }

  private CodeableConcept coverageClass() {
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/coverage-class")
                    .code("group")
                    .build()))
        .build();
  }

  private List<Extension> extensions(FilemanEntry entry) {
    // ToDo update urls (needs to substitute host/base-path per env) and use the correct host
    List<Extension> extensions = new ArrayList<>(2);
    entry
        .internal(InsuranceType.PHARMACY_PERSON_CODE, Integer::valueOf)
        .map(
            value ->
                Extension.builder()
                    .url("http://va.gov/fhir/StructureDefinition/coverage-pharmacyPersonCode")
                    .valueInteger(value)
                    .build())
        .ifPresent(extensions::add);
    entry
        .internal(
            InsuranceType.STOP_POLICY_FROM_BILLING,
            R4CoverageTransformer::stopPolicyFromBillingToBoolean)
        .map(
            value ->
                Extension.builder()
                    .url("http://va.gov/fhir/StructureDefinition/coverage-stopPolicyFromBilling")
                    .valueBoolean(value)
                    .build())
        .ifPresent(extensions::add);
    return extensions.isEmpty() ? null : extensions;
  }

  private Identifier memberId(String memberId) {
    return Identifier.builder()
        .type(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/v2-0203")
                            .code("MB")
                            .display("Member Number")
                            .build()))
                .build())
        .value(memberId)
        .build();
  }

  private Integer order(FilemanEntry entry) {
    return entry.internal(InsuranceType.COORDINATION_OF_BENEFITS, Integer::valueOf).orElse(null);
  }

  private List<Reference> payors(FilemanEntry entry) {
    return entry
        .internal(InsuranceType.INSURANCE_TYPE)
        .map(
            value ->
                RecordCoordinates.builder()
                    .site(site())
                    .file(InsuranceCompany.FILE_NUMBER)
                    .ien(value)
                    .build())
        .map(coords -> toReference("Organization", coords))
        .map(List::of)
        .orElse(null);
  }

  private Period period(FilemanEntry entry) {
    Period period = Period.builder().build();
    entry
        .internal(InsuranceType.EFFECTIVE_DATE_OF_POLICY, this::toFilemanDate)
        .ifPresent(period::start);
    entry
        .internal(InsuranceType.INSURANCE_EXPIRATION_DATE, this::toFilemanDate)
        .ifPresent(period::end);
    if (allBlank(period.start(), period.end())) {
      return null;
    }
    return period;
  }

  @SuppressWarnings("UnnecessaryParentheses")
  CodeableConcept relationship(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    return entry
        .internal(InsuranceType.PT_RELATIONSHIP_HIPAA)
        .flatMap(SubscriberToBeneficiaryRelationship::forCode)
        .map(r -> CodeableConcept.builder().coding(List.of(r.asCoding())).build())
        .orElseThrow(
            () ->
                new UnexpectedVistaValue(
                    InsuranceType.PT_RELATIONSHIP_HIPAA,
                    entry,
                    "Could not map value to a fhir relationship."));
  }

  private String site() {
    return rpcResults.getKey();
  }

  private Coverage toCoverage(LhsLighthouseRpcGatewayResponse.FilemanEntry entry) {
    if (isBlank(entry.fields())) {
      return null;
    }
    return Coverage.builder()
        .id(patientCoordinateStringFrom(patientIcn, site(), entry.ien()))
        .meta(Meta.builder().source(site()).build())
        .extension(extensions(entry))
        .status(Status.active)
        .subscriberId(entry.external(InsuranceType.SUBSCRIBER_ID).orElse(null))
        .beneficiary(beneficiary(patientIcn, entry.external(InsuranceType.PATIENT_ID)))
        .relationship(relationship(entry))
        .period(period(entry))
        .payor(payors(entry))
        .coverageClass(classes(entry))
        .order(order(entry))
        .build();
  }

  /** Transform an RPC response to fhir. */
  public Stream<Coverage> toFhir() {
    return rpcResults.getValue().results().stream()
        .filter(Objects::nonNull)
        .filter(r -> InsuranceType.FILE_NUMBER.equals(r.file()))
        .map(this::toCoverage)
        .filter(Objects::nonNull);
  }

  private String toFilemanDate(String filemanDate) {
    if (filemanDate == null) {
      return null;
    }
    // Reformat to UTC
    return FilemanDate.from(filemanDate, vistaZoneId)
        .instant()
        .atZone(ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_DATE_TIME);
  }
}
