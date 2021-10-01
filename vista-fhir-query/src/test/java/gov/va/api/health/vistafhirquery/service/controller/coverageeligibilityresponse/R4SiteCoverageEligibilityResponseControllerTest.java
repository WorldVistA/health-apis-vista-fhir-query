package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse.CoverageEligibilityResponseSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonRequest;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonResponse;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageSamples;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayListManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PatientId;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class R4SiteCoverageEligibilityResponseControllerTest {
  @Mock CharonClient charon;

  private R4SiteCoverageEligibilityResponseController _controller() {
    return R4SiteCoverageEligibilityResponseController.builder()
        .bundlerFactory(
            R4BundlerFactory.builder()
                .linkProperties(
                    LinkProperties.builder()
                        .defaultPageSize(15)
                        .maxPageSize(100)
                        .publicUrl("http://fugazi.com")
                        .publicR4BasePath("site/{site}/r4")
                        .build())
                .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
                .build())
        .charon(charon)
        .build();
  }

  private RpcInvocationResultV1 _invocationResult(Object value) {
    return RpcInvocationResultV1.builder()
        .vista("123")
        .timezone("UTC")
        .response(json(value))
        .build();
  }

  private <I extends TypeSafeRpcRequest>
      CharonRequest<I, LhsLighthouseRpcGatewayResponse.Results> charonRequestFor(I request) {
    return CharonRequest.<I, LhsLighthouseRpcGatewayResponse.Results>builder()
        .vista("123")
        .rpcRequest(request)
        .responseType(LhsLighthouseRpcGatewayResponse.Results.class)
        .build();
  }

  private <I extends TypeSafeRpcRequest>
      CharonResponse<I, LhsLighthouseRpcGatewayResponse.Results> charonResponseFor(
          CharonRequest<I, LhsLighthouseRpcGatewayResponse.Results> request,
          LhsLighthouseRpcGatewayResponse.Results results) {
    return CharonResponse.<I, LhsLighthouseRpcGatewayResponse.Results>builder()
        .request(request)
        .invocationResult(_invocationResult(results))
        .value(results)
        .build();
  }

  @Test
  void searchByPatient() {
    var httpRequest = requestFromUri("?_count=15&patient=p1");
    // Insurance Type
    var insTypeRequest =
        charonRequestFor(
            LhsLighthouseRpcGatewayCoverageSearch.Request.builder()
                .id(PatientId.forIcn("p1"))
                .build());
    var insTypeResults =
        CoverageSamples.VistaLhsLighthouseRpcGateway.create().getsManifestResults("c1");
    when(charon.request(eq(insTypeRequest)))
        .thenReturn(charonResponseFor(insTypeRequest, insTypeResults));
    // Plan Limitations
    var limitationsRequest =
        charonRequestFor(
            LhsLighthouseRpcGatewayListManifest.Request.builder()
                .file(PlanCoverageLimitations.FILE_NUMBER)
                .fields(List.of("@", "#.01IE"))
                .build());
    var limitationsResults =
        CoverageEligibilityResponseSamples.VistaLhsLighthouseRpcGateway.create()
            .getsManifestResults();
    when(charon.request(eq(limitationsRequest)))
        .thenReturn(charonResponseFor(limitationsRequest, limitationsResults));
    var actual = _controller().coverageEligibilityResponseSearch(httpRequest, "123", "p1", 15);
    var expected =
        CoverageEligibilityResponseSamples.R4.asBundle(
            "http://fugazi.com/site/123/r4",
            List.of(CoverageEligibilityResponseSamples.R4.create().coverageEligibilityResponse()),
            1,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/site/123/r4/CoverageEligibilityResponse",
                "_count=15&patient=p1"));
    actual.entry().forEach(e -> e.resource().created("ignored"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }
}
