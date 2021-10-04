package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.asCodeableConcept;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;

import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.DataAbsentReason.Reason;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Insurance;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Item;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Outcome;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Purpose;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Status;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.FilemanDate;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.UnexpectedVistaValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class R4CoverageEligibilityResponseTransformer {
  /** The fields needed by the transformer to generate a fhir response from the vista file. */
  public static final List<String> REQUIRED_FIELDS =
      List.of(
          PlanCoverageLimitations.COVERAGE_CATEGORY,
          PlanCoverageLimitations.COVERAGE_STATUS,
          PlanCoverageLimitations.EFFECTIVE_DATE,
          PlanCoverageLimitations.PLAN);

  @NonNull R4CoverageEligibilityResponseSearchContext searchContext;

  @Builder.Default ZoneId vistaZoneId = ZoneOffset.UTC;

  private Period benefitPeriod(Optional<String> effectiveDate) {
    return effectiveDate
        .map(
            filemanDate ->
                FilemanDate.from(filemanDate, vistaZoneId)
                    .instant()
                    .atZone(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_DATE_TIME))
        .map(d -> Period.builder().start(d).build())
        .orElse(null);
  }

  private Coding codingForVistaCode(String system, String code) {
    return Coding.builder().system(system).code(code).build();
  }

  private Insurance insurance(FilemanEntry coverage, FilemanEntry planLimitation) {
    return Insurance.builder()
        .coverage(
            toReference(
                "Coverage",
                PatientTypeCoordinates.builder()
                    .siteId(searchContext().site())
                    .icn(searchContext().patientIcn())
                    .recordId(coverage.ien())
                    .build()))
        .inforce(
            isCoverageInForce(planLimitation.external(PlanCoverageLimitations.COVERAGE_STATUS)))
        .benefitPeriod(
            benefitPeriod(planLimitation.internal(PlanCoverageLimitations.EFFECTIVE_DATE)))
        .item(List.of(insuranceItem(planLimitation)))
        .build();
  }

  private Item insuranceItem(FilemanEntry planLimitation) {
    var itemCategory =
        planLimitation
            .external(PlanCoverageLimitations.COVERAGE_CATEGORY)
            .map(
                category ->
                    codingForVistaCode(
                        CoverageEligibilityResponseStructureDefinitions.PLAN_LIMITATION_CATEGORY,
                        category))
            .orElse(null);
    var item = Item.builder().category(asCodeableConcept(itemCategory)).build();
    if (isLimitationConditional(planLimitation)) {
      /*
       * ToDo https://vajira.max.gov/browse/API-10417
       * Description will always be null until Limitation Comment is added to REQUIRED_FIELDS
       * The list rpc does not support the Limitation Comment field (it is a multiple)
       */
      item.description(
          planLimitation.external(PlanCoverageLimitations.LIMITATION_COMMENT).orElse(null));
      item.excluded(true);
    }
    return item;
  }

  private Reference insurer(Optional<String> field) {
    return field
        .map(
            value ->
                RecordCoordinates.builder()
                    .site(searchContext().site())
                    .file(InsuranceCompany.FILE_NUMBER)
                    .ien(value)
                    .build())
        .map(coords -> toReference("Organization", coords))
        .orElse(null);
  }

  private Boolean isCoverageInForce(Optional<String> coverageStatus) {
    if (coverageStatus.isEmpty()) {
      return null;
    }
    switch (coverageStatus.get()) {
      case "NOT COVERED":
        return false;
      case "COVERED":
        // fall through
      case "CONDITIONAL COVERAGE":
        return true;
      default:
        throw new UnexpectedVistaValue(
            PlanCoverageLimitations.COVERAGE_STATUS, coverageStatus.get(), "Unknown value.");
    }
  }

  private boolean isLimitationConditional(FilemanEntry planLimitation) {
    return planLimitation
        .external(PlanCoverageLimitations.COVERAGE_STATUS)
        .map("CONDITIONAL COVERAGE"::equals)
        .orElse(false);
  }

  Stream<CoverageEligibilityResponse> toCoverageEligibilityResponse(FilemanEntry coverage) {
    var plan =
        coverage
            .internal(InsuranceType.GROUP_PLAN)
            .orElseThrow(
                () ->
                    new UnexpectedVistaValue(
                        InsuranceType.GROUP_PLAN,
                        null,
                        "Cannot determine plan for eligibilities."));
    return searchContext().planLimitationsResults().results().stream()
        .filter(r -> plan.equals(r.internal(PlanCoverageLimitations.PLAN).orElse(null)))
        .map(
            pcl ->
                CoverageEligibilityResponse.builder()
                    .meta(Meta.builder().source(searchContext().site()).build())
                    .id(searchContext().patientTypeCoordinatesFor(coverage.ien()).toString())
                    .status(Status.active)
                    .purpose(List.of(Purpose.benefits, Purpose.discovery))
                    .patient(toReference("Patient", searchContext().patientIcn(), null))
                    .created(Instant.now().toString())
                    ._request(DataAbsentReason.of(Reason.unsupported))
                    .outcome(Outcome.complete)
                    .insurer(insurer(coverage.internal(InsuranceType.INSURANCE_TYPE)))
                    .insurance(List.of(insurance(coverage, pcl)))
                    .build());
  }

  public Stream<CoverageEligibilityResponse> toFhir() {
    return searchContext().coverageResults().results().stream()
        .flatMap(this::toCoverageEligibilityResponse);
  }
}
