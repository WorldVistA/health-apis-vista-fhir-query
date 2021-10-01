package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toReference;

import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.DataAbsentReason.Reason;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Outcome;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Purpose;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Status;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.UnexpectedVistaValue;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import java.time.Instant;
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
  public static final List<String> REQUIRED_FIELDS = List.of(PlanCoverageLimitations.PLAN);

  @NonNull R4CoverageEligibilityResponseSearchContext searchContext;

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
                    .id(searchContext().resourceIdFor(pcl.ien()).toString())
                    .status(Status.active)
                    .purpose(List.of(Purpose.discovery))
                    .patient(toReference("Patient", searchContext().patientIcn(), null))
                    .created(Instant.now().toString())
                    ._request(DataAbsentReason.of(Reason.unsupported))
                    .outcome(Outcome.complete)
                    .insurer(insurer(coverage.internal(InsuranceType.INSURANCE_TYPE)))
                    .build());
  }

  public Stream<CoverageEligibilityResponse> toFhir() {
    return searchContext().coverageResults().results().stream()
        .flatMap(this::toCoverageEligibilityResponse);
  }
}
