package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.recordCoordinatesForReference;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.autoincrement;
import static gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory.index;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.CoverageClass;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
  private static final WriteableFilemanValueFactory filemanValue =
      WriteableFilemanValueFactory.forFile(InsuranceType.FILE_NUMBER);

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
    return filemanValue.forRequiredString(InsuranceType.COORDINATION_OF_BENEFITS, 1, priority);
  }

  private Optional<Extension> extensionForSystem(String system) {
    if (isBlank(coverage().extension())) {
      return Optional.empty();
    }
    return coverage().extension().stream().filter(e -> system.equals(e.url())).findFirst();
  }

  WriteableFilemanValue groupPlan(List<CoverageClass> coverageTypes) {
    if (isBlank(coverageTypes)) {
      throw BadRequestPayload.because(InsuranceType.GROUP_PLAN, ".class is null");
    }
    var filteredCoverageTypes =
        coverageTypes.stream()
            .filter(
                c -> {
                  if (c.type() == null) {
                    return false;
                  }
                  return c.type().coding().stream()
                      .anyMatch(coding -> "group".equals(coding.code()));
                })
            .collect(toList());
    if (filteredCoverageTypes.size() != 1) {
      throw BadRequestPayload.because(
          InsuranceType.GROUP_PLAN,
          "Expected 1 .class for type `group`, but got " + filteredCoverageTypes.size());
    }
    // Group slice cardinality is 0..1
    return filteredCoverageTypes.stream()
        .findFirst()
        .flatMap(
            c -> recordCoordinatesForReference(Reference.builder().reference(c.value()).build()))
        .map(filemanValue.recordCoordinatesToPointer(GroupInsurancePlan.FILE_NUMBER, index(1)))
        .orElseThrow(
            () ->
                BadRequestPayload.because(
                    InsuranceType.GROUP_PLAN, ".class type 'group' id is unknown/invalid"));
  }

  WriteableFilemanValue insuranceType(List<Reference> payors) {
    if (isBlank(payors)) {
      throw BadRequestPayload.because(InsuranceType.INSURANCE_TYPE, "payor is null");
    }
    if (payors.size() != 1) {
      throw BadRequestPayload.because(
          InsuranceType.INSURANCE_TYPE, "Expected 1 payor, but got " + payors.size());
    }
    // Cardinality 1..* but vista expects a single pointer
    return IntStream.range(0, payors.size())
        .mapToObj(
            index ->
                recordCoordinatesForReference(payors.get(index))
                    .map(
                        filemanValue.recordCoordinatesToPointer(
                            InsuranceCompany.FILE_NUMBER, autoincrement()))
                    .orElse(null))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(
            () -> BadRequestPayload.because(InsuranceType.INSURANCE_TYPE, "payor not found"));
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
    return filemanValue.forRequiredIdentifier(
        InsuranceType.PATIENT_ID, 1, beneficiary.identifier());
  }

  WriteableFilemanValue patientRelationshipHipaa(CodeableConcept relationship) {
    if (isBlank(relationship) || isBlank(relationship.coding())) {
      throw BadRequestPayload.because(InsuranceType.PT_RELATIONSHIP_HIPAA, "relationship is null");
    }
    if (relationship.coding().size() != 1) {
      throw BadRequestPayload.because(
          InsuranceType.PT_RELATIONSHIP_HIPAA,
          "Expected 1 relationship code, but got " + relationship.coding().size());
    }
    /* TODO https://vajira.max.gov/browse/API-11160 verify system is http://terminology.hl7.org/CodeSystem/subscriber-relationship */
    return relationship.coding().stream()
        .map(SubscriberToBeneficiaryRelationship::fromCoding)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .map(
            filemanValue.toString(
                InsuranceType.PT_RELATIONSHIP_HIPAA,
                index(1),
                SubscriberToBeneficiaryRelationship::display))
        .orElseThrow(
            () ->
                BadRequestPayload.because(
                    InsuranceType.PT_RELATIONSHIP_HIPAA,
                    "SubscriberToBeneficiary code not found."));
  }

  List<WriteableFilemanValue> policyStartAndEndDates(Period period) {
    if (isBlank(period) || isBlank(period.start())) {
      throw BadRequestPayload.because(
          InsuranceType.EFFECTIVE_DATE_OF_POLICY, "period start is null");
    }
    List<WriteableFilemanValue> dates = new ArrayList<>(2);
    var start = Instant.parse(period.start());
    var effectiveDate =
        filemanValue.forRequiredString(
            InsuranceType.EFFECTIVE_DATE_OF_POLICY, 1, vistaDateFormatter().format(start));
    dates.add(effectiveDate);
    if (isBlank(period.end())) {
      return dates;
    }
    var end = Instant.parse(period.end());
    /* TODO https://vajira.max.gov/browse/API-11160 check end is _after_ the start, isBefore will allow for equal start and end */
    if (end.isBefore(start)) {
      throw BadRequestPayload.because("Coverage expiration Date is before start date.");
    }
    /* When expiration date (conditionally required) is present,
     * any failure to process it should result in failure. */
    var expire =
        filemanValue.forRequiredString(
            InsuranceType.INSURANCE_EXPIRATION_DATE, 1, vistaDateFormatter().format(end));
    dates.add(expire);
    return dates;
  }

  WriteableFilemanValue stopPolicyFromBilling(Extension extension) {
    return filemanValue.forRequiredBoolean(
        InsuranceType.STOP_POLICY_FROM_BILLING, 1, extension, value -> value ? "YES" : "NO");
  }

  WriteableFilemanValue subscriberId(String subscriberId) {
    if (isBlank(subscriberId)) {
      throw BadRequestPayload.because(InsuranceType.SUBSCRIBER_ID, "subscriberId is null");
    }
    return filemanValue.forRequiredString(InsuranceType.SUBSCRIBER_ID, 1, subscriberId);
  }

  /** Create a set of writeable fileman values. */
  public Set<WriteableFilemanValue> toInsuranceTypeFile() {
    /* TODO https://vajira.max.gov/browse/API-11160 verify status is active */
    Set<WriteableFilemanValue> fields = new HashSet<>();
    Optional.ofNullable(coverage().id())
        .map(PatientTypeCoordinates::fromString)
        .map(filemanValue.patientTypeCoordinatesToPointer(InsuranceType.FILE_NUMBER, index(1)))
        .ifPresent(fields::add);
    fields.add(insuranceType(coverage().payor()));
    fields.add(groupPlan(coverage().coverageClass()));
    fields.add(coordinationOfBenefits(coverage().order()));
    extensionForSystem(CoverageStructureDefinitions.STOP_POLICY_FROM_BILLING)
        .map(this::stopPolicyFromBilling)
        .ifPresentOrElse(
            fields::add,
            () -> {
              throw BadRequestPayload.because(
                  InsuranceType.STOP_POLICY_FROM_BILLING,
                  "extension not found: " + CoverageStructureDefinitions.STOP_POLICY_FROM_BILLING);
            });
    fields.add(patientRelationshipHipaa(coverage().relationship()));
    extensionForSystem(CoverageStructureDefinitions.PHARMACY_PERSON_CODE)
        .map(filemanValue.extensionToInteger(InsuranceType.PHARMACY_PERSON_CODE, index(1)))
        .ifPresent(fields::add);
    fields.add(patientId(coverage().beneficiary()));
    fields.add(subscriberId(coverage().subscriberId()));
    fields.addAll(policyStartAndEndDates(coverage().period()));
    return fields;
  }
}
