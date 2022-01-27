package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(staticName = "create")
public class R4CoverageEligibilityResponseSearchContext {
  private String site;

  private String patientIcn;

  private LhsLighthouseRpcGatewayResponse.Results coverageResults;

  private LhsLighthouseRpcGatewayResponse.Results planLimitationsResults;

  /** Build patient type coordinates for a given ien from the insurance type file. */
  public PatientTypeCoordinates patientTypeCoordinatesFor(String ien) {
    return PatientTypeCoordinates.builder()
        .site(site())
        .icn(patientIcn())
        .file(InsuranceType.FILE_NUMBER)
        .ien(ien)
        .build();
  }
}
