package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CoverageEligibilityResponseSamples {
  @NoArgsConstructor(staticName = "create")
  public static class R4 {
    public CoverageEligibilityResponse coverageEligibilityResponse() {
      return coverageEligibilityResponse("p1", "87");
    }

    public CoverageEligibilityResponse coverageEligibilityResponse(
        String patient, String organization) {
      return CoverageEligibilityResponse.builder()
          .status(CoverageEligibilityResponse.Status.active)
          .purpose(List.of(CoverageEligibilityResponse.Purpose.discovery))
          .patient(Reference.builder().reference("Patient/" + patient).build())
          .created("ignored")
          ._request(DataAbsentReason.of(DataAbsentReason.Reason.unsupported))
          .outcome(CoverageEligibilityResponse.Outcome.complete)
          .insurer(
              Reference.builder()
                  .reference(
                      "Organization/"
                          + RecordCoordinates.builder()
                              .site("123")
                              .file(InsuranceCompany.FILE_NUMBER)
                              .ien(organization)
                              .build()
                              .toString())
                  .build())
          .build();
    }
  }

  @NoArgsConstructor(staticName = "create")
  public static class VistaLhsLighthouseRpcGateway {
    private Map<String, LhsLighthouseRpcGatewayResponse.Values> fields() {
      Map<String, LhsLighthouseRpcGatewayResponse.Values> fields = new HashMap<>();
      fields.put(
          PlanCoverageLimitations.PLAN,
          LhsLighthouseRpcGatewayResponse.Values.of("BCBS OF FL", "87"));
      return Map.copyOf(fields);
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults() {
      return getsManifestResults("8");
    }

    public LhsLighthouseRpcGatewayResponse.Results getsManifestResults(String id) {
      return LhsLighthouseRpcGatewayResponse.Results.builder()
          .results(
              List.of(
                  LhsLighthouseRpcGatewayResponse.FilemanEntry.builder()
                      .file(PlanCoverageLimitations.FILE_NUMBER)
                      .ien(id)
                      .fields(fields())
                      .build()))
          .build();
    }
  }
}
