package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.recordCoordinatesForReference;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.CoverageClass;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
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
import java.util.stream.IntStream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class R4CoverageToInsuranceTypeFileTransformer {
  @NonNull Coverage coverage;

  DateTimeFormatter vistaDateFormatter;

  @Builder
  R4CoverageToInsuranceTypeFileTransformer(@NonNull Coverage coverage, ZoneId timezone) {
    this.coverage = coverage;
    this.vistaDateFormatter =
        DateTimeFormatter.ofPattern("MM-dd-yyy")
            .withZone(timezone == null ? ZoneId.of("UTC") : timezone);
  }

  @SuppressWarnings("UnnecessaryParentheses")
  WriteableFilemanValue coordinationOfBenefits(Integer order) {
    if (isBlank(order)) {
      throw BadRequestPayload.because(InsuranceType.COORDINATION_OF_BENEFITS, "order is null");
    }
    var priority =
        switch (order) {
          case 1 -> "PRIMARY";
          case 2 -> "SECONDARY";
          case 3 -> "TERTIARY";
          default -> throw new IllegalArgumentException("Unexpected order value: " + order);
        };
    return insuranceTypeCoordinates(InsuranceType.COORDINATION_OF_BENEFITS, 1, priority);
  }

  WriteableFilemanValue effectiveDateOfPolicy(Period period) {
    if (isBlank(period) || isBlank(period.start())) {
      throw BadRequestPayload.because(
          InsuranceType.EFFECTIVE_DATE_OF_POLICY, "period start is null");
    }
    var date = Instant.parse(period.start());
    return insuranceTypeCoordinates(
        InsuranceType.EFFECTIVE_DATE_OF_POLICY, 1, vistaDateFormatter().format(date));
  }

  private Optional<Extension> extensionForSystem(String system) {
    if (isBlank(coverage().extension())) {
      return Optional.empty();
    }
    return coverage().extension().stream().filter(e -> system.equals(e.url())).findFirst();
  }

  WriteableFilemanValue groupPlan(List<CoverageClass> coverageTypes) {
    if (isBlank(coverageTypes)) {
      throw BadRequestPayload.because(InsuranceType.GROUP_PLAN, "class is null");
    }
    // Group slice cardinality is 0..1
    return coverageTypes.stream()
        .filter(
            c -> {
              if (c.type() == null) {
                return false;
              }
              return c.type().coding().stream().anyMatch(coding -> "group".equals(coding.code()));
            })
        .map(
            c ->
                recordCoordinatesForReference(Reference.builder().reference(c.value()).build())
                    .orElse(null))
        .findFirst()
        .map(id -> pointer(GroupInsurancePlan.FILE_NUMBER, 1, id.ien()))
        .orElseThrow(
            () ->
                BadRequestPayload.because(
                    InsuranceType.GROUP_PLAN, "class type 'group' not found"));
  }

  Optional<WriteableFilemanValue> insuranceExpirationDate(Period period) {
    if (isBlank(period)) {
      return Optional.empty();
    }
    var date = Instant.parse(period.end());
    return Optional.ofNullable(
        insuranceTypeCoordinates(
            InsuranceType.INSURANCE_EXPIRATION_DATE, 1, vistaDateFormatter().format(date)));
  }

  WriteableFilemanValue insuranceType(List<Reference> payors) {
    if (isBlank(payors)) {
      throw BadRequestPayload.because(InsuranceType.INSURANCE_TYPE, "payor is null");
    }
    // Cardinality 1..* but vista expects a single pointer
    return IntStream.range(0, payors.size())
        .mapToObj(
            index -> {
              var recordCoordinates = recordCoordinatesForReference(payors.get(index));
              return recordCoordinates.isEmpty()
                  ? null
                  : pointer(InsuranceCompany.FILE_NUMBER, index + 1, recordCoordinates.get().ien());
            })
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(
            () -> BadRequestPayload.because(InsuranceType.INSURANCE_TYPE, "payor not found"));
  }

  private WriteableFilemanValue insuranceTypeCoordinates(
      String field, Integer index, String value) {
    return WriteableFilemanValue.builder()
        .file(InsuranceType.FILE_NUMBER)
        .field(field)
        .index(index)
        .value(value)
        .build();
  }

  WriteableFilemanValue patientId(Reference beneficiary) {
    if (isBlank(beneficiary) || isBlank(beneficiary.identifier())) {
      throw BadRequestPayload.because(InsuranceType.PATIENT_ID, "beneficiary identifier is null");
    }
    var isMemberId =
        Optional.ofNullable(beneficiary.identifier().type()).map(CodeableConcept::coding).stream()
            .flatMap(Collection::stream)
            .anyMatch(coding -> "MB".equals(coding.code()));
    if (!isMemberId) {
      throw BadRequestPayload.because(InsuranceType.PATIENT_ID, "identifier of type MB not found");
    }
    var memberId = beneficiary.identifier().value();
    return insuranceTypeCoordinates(InsuranceType.PATIENT_ID, 1, memberId);
  }

  WriteableFilemanValue patientRelationshipHipaa(CodeableConcept relationship) {
    if (isBlank(relationship) || isBlank(relationship.coding())) {
      throw BadRequestPayload.because(InsuranceType.PT_RELATIONSHIP_HIPAA, "relationship is null");
    }
    if (relationship.coding().size() != 1) {
      throw BadRequestPayload.because(
          InsuranceType.PT_RELATIONSHIP_HIPAA,
          "Unexpected relationship code count: " + relationship.coding().size());
    }
    var maybeCode =
        relationship.coding().stream()
            .map(SubscriberToBeneficiaryRelationship::fromCoding)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    var relCode =
        maybeCode.orElseThrow(
            () ->
                BadRequestPayload.because(
                    InsuranceType.PT_RELATIONSHIP_HIPAA,
                    "SubscriberToBeneficiary code not found."));
    return insuranceTypeCoordinates(InsuranceType.PT_RELATIONSHIP_HIPAA, 1, relCode.display());
  }

  Optional<WriteableFilemanValue> pharmacyPersonCode(Extension pharmacyPersonCode) {
    if (isBlank(pharmacyPersonCode)) {
      return Optional.empty();
    }
    return Optional.ofNullable(
        insuranceTypeCoordinates(
            InsuranceType.PHARMACY_PERSON_CODE, 1, "" + pharmacyPersonCode.valueInteger()));
  }

  private WriteableFilemanValue pointer(@NonNull String file, int index, String ien) {
    if (isBlank(ien)) {
      return null;
    }
    return WriteableFilemanValue.builder().file(file).field("ien").index(index).value(ien).build();
  }

  WriteableFilemanValue stopPolicyFromBilling(Extension extension) {
    if (isBlank(extension) || isBlank(extension.valueBoolean())) {
      throw BadRequestPayload.because(InsuranceType.STOP_POLICY_FROM_BILLING, "extension is null");
    }
    return insuranceTypeCoordinates(
        InsuranceType.STOP_POLICY_FROM_BILLING, 1, extension.valueBoolean() ? "YES" : "NO");
  }

  WriteableFilemanValue subscriberId(String subscriberId) {
    if (isBlank(subscriberId)) {
      throw BadRequestPayload.because(InsuranceType.SUBSCRIBER_ID, "subscriberId is null");
    }
    return insuranceTypeCoordinates(InsuranceType.SUBSCRIBER_ID, 1, subscriberId);
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceTypeFile() {
    Set<WriteableFilemanValue> fields = new HashSet<>();
    fields.add(insuranceType(coverage().payor()));
    fields.add(groupPlan(coverage().coverageClass()));
    fields.add(coordinationOfBenefits(coverage().order()));
    insuranceExpirationDate(coverage().period()).ifPresent(fields::add);
    extensionForSystem("http://va.gov/fhir/StructureDefinition/coverage-stopPolicyFromBilling")
        .map(this::stopPolicyFromBilling)
        .ifPresentOrElse(
            fields::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceType.STOP_POLICY_FROM_BILLING, "extension not found");
            });
    fields.add(patientRelationshipHipaa(coverage().relationship()));
    extensionForSystem("http://va.gov/fhir/StructureDefinition/coverage-pharmacyPersonCode")
        .flatMap(this::pharmacyPersonCode)
        .ifPresent(fields::add);
    fields.add(patientId(coverage().beneficiary()));
    fields.add(subscriberId(coverage().subscriberId()));
    fields.add(effectiveDateOfPolicy(coverage().period()));
    return fields;
  }
}
