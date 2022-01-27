package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.invocationResultV1;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
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

  private R4SiteCoverageController _insuranceTypeCoverageController() {
    return R4SiteCoverageController.builder()
        .bundlerFactory(_bundlerFactory())
        .charon(mockCharon)
        .witnessProtection(mockWitnessProtection)
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
    var coverageSample = CoverageSamples.R4.create().bufferCoverage("123", "cov1", "p1");
    mockWitnessProtection.add("public-cov1", "p1+123+355.33+cov1");
    var response = new MockHttpServletResponse();
    _insuranceBufferCoverageController().coverageCreate(response, "123", coverageSample);
    assertThat(captor.getValue().rpcRequest().api()).isEqualTo(CoverageWriteApi.CREATE);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeader(HttpHeaders.LOCATION))
        .isEqualTo("http://fugazi.com/hcs/123/r4/Coverage/public-cov1");
  }

  @Test
  void coverageCreateUsingInsuranceTypeControllerHack() {
    var results =
        CoverageSamples.VistaLhsLighthouseRpcGateway.create().createInsuranceBufferResults("cov1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(invocationResultV1(results)).build();
    when(mockCharon.request(captor.capture())).thenAnswer(answer);
    var coverageSample = CoverageSamples.R4.create().bufferCoverage("123", "cov1", "p1");
    mockWitnessProtection.add("public-cov1", "p1+123+355.33+cov1");
    var response = new MockHttpServletResponse();
    _insuranceTypeCoverageController().coverageCreate(response, "123", true, coverageSample);
    assertThat(captor.getValue().rpcRequest().api()).isEqualTo(CoverageWriteApi.CREATE);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeader(HttpHeaders.LOCATION))
        .isEqualTo("http://fugazi.com/hcs/123/r4/Coverage/public-cov1");
  }

  @Test
  void coverageRead() {
    assertThat(_insuranceBufferCoverageController().coverageRead("123", "fake-id"))
        .isEqualTo(Coverage.builder().id("shanktopus").status(Coverage.Status.draft).build());
  }
}
