package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

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
}
