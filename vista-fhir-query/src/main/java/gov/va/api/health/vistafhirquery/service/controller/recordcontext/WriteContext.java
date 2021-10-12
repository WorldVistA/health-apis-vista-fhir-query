package gov.va.api.health.vistafhirquery.service.controller.recordcontext;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;

public interface WriteContext<BodyT extends IsResource> {
  BodyT body();

  CoverageWriteApi coverageWriteApi();

  String fileNumber();

  LhsLighthouseRpcGatewayResponse.FilemanEntry result();

  void result(LhsLighthouseRpcGatewayResponse response);

  String site();
}
