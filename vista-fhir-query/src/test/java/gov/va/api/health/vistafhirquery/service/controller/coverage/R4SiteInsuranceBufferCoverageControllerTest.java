package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.invocationResultV1;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.talos.ResponseIncludesIcnHeaderAdvice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class R4SiteInsuranceBufferCoverageControllerTest {
  @Mock CharonClient mockCharon;

  MockWitnessProtection mockWitnessProtection = new MockWitnessProtection();

  private R4BundlerFactory _bundlerFactory() {
    return R4BundlerFactory.builder()
        .linkProperties(
            LinkProperties.builder()
                .defaultPageSize(15)
                .maxPageSize(100)
                .publicUrl("http://fugazi.com")
                .publicR4BasePath("hcs/{site}/r4")
                .build())
        .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
        .build();
  }

  private R4SiteInsuranceBufferCoverageController _insuranceBufferCoverageController() {
    return R4SiteInsuranceBufferCoverageController.builder()
        .bundlerFactory(_bundlerFactory())
        .charon(mockCharon)
        .witnessProtection(mockWitnessProtection)
        .build();
  }

  private RpcInvocationResultV1 _invocationResult(Object value) {
    return RpcInvocationResultV1.builder()
        .vista("123")
        .timezone("UTC")
        .response(json(value))
        .build();
  }

  @Test
  void coverageCreate() {
    var results =
        CoverageSamples.VistaLhsLighthouseRpcGateway.create().createInsuranceBufferResults("cov1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(invocationResultV1(results)).build();
    when(mockCharon.request(captor.capture())).thenAnswer(answer);
    var coverageSample =
        CoverageSamples.R4.create().coverageInsuranceBufferRead("p1", "123", "cov1");
    mockWitnessProtection.add("public-cov1", "p1+123+355.33+cov1");
    var response = new MockHttpServletResponse();
    _insuranceBufferCoverageController().coverageCreate(response, "123", coverageSample);
    assertThat(captor.getValue().rpcRequest().api()).isEqualTo(CoverageWriteApi.CREATE);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeader(HttpHeaders.LOCATION))
        .isEqualTo("http://fugazi.com/hcs/123/r4/Coverage/public-cov1");
    assertThat(response.getHeader(ResponseIncludesIcnHeaderAdvice.INCLUDES_ICN_HEADER))
        .isEqualTo("p1");
  }

  @Test
  void coverageRead() {
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createInsuranceBufferResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(mockCharon.request(captor.capture())).thenAnswer(answer);
    mockWitnessProtection.add("pubCover1", "p1+123+355.33+cov1");
    var actual = _insuranceBufferCoverageController().coverageRead("123", "pubCover1");
    var expected = CoverageSamples.R4.create().coverageInsuranceBufferRead("p1", "123", "ip1");
    CoverageSamples.R4.cleanUpContainedReferencesForComparison(actual);
    CoverageSamples.R4.cleanUpContainedReferencesForComparison(expected);
    assertThat(json(actual)).isEqualTo(json(expected));
    var request = captor.getValue();
    assertThat(request.vista()).isEqualTo("123");
  }
}
