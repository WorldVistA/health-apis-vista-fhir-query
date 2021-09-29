package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.vistafhirquery.service.api.R4CoverageEligibilityResponseApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonResponse;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PatientId;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Builder
@Validated
@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@RequestMapping(produces = {"application/json", "application/fhir+json"})
public class R4SiteCoverageEligibilityResponseController
    implements R4CoverageEligibilityResponseApi {

  private final R4BundlerFactory bundlerFactory;

  /** Search support. */
  @Override
  @GetMapping("/site/{site}/r4/CoverageEligibilityResponse")
  public CoverageEligibilityResponse.Bundle coverageEligibilityResponseSearch(
      @Redact HttpServletRequest httpRequest,
      @NonNull @PathVariable(value = "site") String site,
      @RequestParam(value = "patient") String icn,
      @RequestParam(value = "_count", required = false) Integer count) {
    var searchByPatient =
        LhsLighthouseRpcGatewayCoverageSearch.Request.builder().id(PatientId.forIcn(icn)).build();
    var charonRequest = lighthouseRpcGatewayRequest(site, searchByPatient);
    var charonResponse =
        CharonResponse
            .<LhsLighthouseRpcGatewayCoverageSearch.Request,
                LhsLighthouseRpcGatewayResponse.Results>
                builder()
            .request(charonRequest)
            .invocationResult(RpcInvocationResultV1.builder().vista(site).response("").build())
            .value(
                LhsLighthouseRpcGatewayResponse.Results.builder()
                    .results(
                        List.of(LhsLighthouseRpcGatewayResponse.FilemanEntry.builder().build()))
                    .build())
            .build();
    return toBundle(httpRequest, charonResponse)
        .apply(lighthouseRpcGatewayResponse(charonResponse));
  }

  private R4Bundler<
          LhsLighthouseRpcGatewayResponse,
          CoverageEligibilityResponse,
          CoverageEligibilityResponse.Entry,
          CoverageEligibilityResponse.Bundle>
      toBundle(
          HttpServletRequest request,
          CharonResponse<
                  LhsLighthouseRpcGatewayCoverageSearch.Request,
                  LhsLighthouseRpcGatewayResponse.Results>
              response) {
    return bundlerFactory
        .forTransformation(transformation())
        .site(response.invocationResult().vista())
        .bundling(
            R4Bundling.newBundle(CoverageEligibilityResponse.Bundle::new)
                .newEntry(CoverageEligibilityResponse.Entry::new)
                .build())
        .resourceType("CoverageEligibilityResponse")
        .request(request)
        .build();
  }

  private R4Transformation<LhsLighthouseRpcGatewayResponse, CoverageEligibilityResponse>
      transformation() {
    return R4Transformation.<LhsLighthouseRpcGatewayResponse, CoverageEligibilityResponse>builder()
        .toResource(rpcResponse -> List.of())
        .build();
  }
}
