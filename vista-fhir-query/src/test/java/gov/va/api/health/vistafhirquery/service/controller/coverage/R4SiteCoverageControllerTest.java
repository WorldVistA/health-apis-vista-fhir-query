package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class R4SiteCoverageControllerTest {
  @Mock CharonClient charon;

  @Mock WitnessProtection witnessProtection;

  private R4SiteCoverageController _controller() {
    return R4SiteCoverageController.builder()
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
        .witnessProtection(witnessProtection)
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
  void create() {
    var response = new MockHttpServletResponse();
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createCoverageResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    when(witnessProtection.toPublicId(Coverage.class, "p1+123+ip1")).thenReturn("public-ip1");
    _controller()
        .coverageCreate(
            response, "123", CoverageSamples.R4.create().coverage("123", "not-used", "p1"));
    assertThat(captor.getValue().rpcRequest().api()).isEqualTo(CoverageWriteApi.CREATE);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeader("Location"))
        .isEqualTo("http://fugazi.com/site/123/r4/Coverage/public-ip1");
  }

  @Test
  void read() {
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    when(witnessProtection.privateIdForResourceOrDie("pubCover1", Coverage.class))
        .thenReturn(
            PatientTypeCoordinates.builder()
                .icn("p1")
                .siteId("123")
                .recordId("ip1")
                .build()
                .toString());
    var actual = _controller().coverageRead("123", "pubCover1");
    var expected = CoverageSamples.R4.create().coverage("123", "ip1", "p1");
    assertThat(json(actual)).isEqualTo(json(expected));
    var request = captor.getValue();
    assertThat(request.vista()).isEqualTo("123");
  }

  @Test
  void searchByPatientWithResults() {
    var httpRequest = requestFromUri("?_count=10&patient=p1");
    var results = CoverageSamples.VistaLhsLighthouseRpcGateway.create().getsManifestResults();
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageSearch.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    var actual = _controller().coverageSearch(httpRequest, "123", "p1", 1, 10);
    var expected =
        CoverageSamples.R4.asBundle(
            "http://fugazi.com/site/123/r4",
            List.of(CoverageSamples.R4.create().coverage("123", "1,8,", "p1")),
            1,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/site/123/r4/Coverage",
                "_count=10&patient=p1"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchByPatientWithoutResults() {
    var httpRequest = requestFromUri("?page=1&_count=10&patient=p1");
    var results = LhsLighthouseRpcGatewayResponse.Results.builder().build();
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageSearch.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    var actual = _controller().coverageSearch(httpRequest, "123", "p1", 1, 10);
    var expected =
        CoverageSamples.R4.asBundle(
            "http://fugazi.com/site/123/r4",
            List.of(),
            0,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/site/123/r4/Coverage",
                "page=1&_count=10&patient=p1"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void update() {
    var response = new MockHttpServletResponse();
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createCoverageResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    // TODO API-10150-put-coverage-part-2
    // when(charon.request(captor.capture())).thenAnswer(answer);
    // when(witnessProtection.toPublicId(Coverage.class, "p1+123+ip1")).thenReturn("public-ip1");
    _controller()
        .coverageUpdate(
            response,
            "123",
            "public-ip1",
            CoverageSamples.R4.create().coverage("123", "ip1", "p1"));
    // assertThat(captor.getValue().rpcRequest().api()).isEqualTo(CoverageWriteApi.UPDATE);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  @Disabled
  void updateThrowsBadRequestPayloadForResourcesThatCannotBeProcessed() {
    // 422
    fail();
  }

  @Test
  @Disabled
  void updateThrowsCannotUpdateResourceWithMismatchedIdsWhenUrlAndPayloadIdsDoNotMatch() {
    // 400
    // when /r4/Coverage/123 != resource.id
    // when /r4/Coverage/123 && resource.id == null
    fail();
  }

  @Test
  @Disabled
  void updateThrowsCannotUpdateUnknownResourceForUnknownResource() {
    // 405
    fail();
  }
}
