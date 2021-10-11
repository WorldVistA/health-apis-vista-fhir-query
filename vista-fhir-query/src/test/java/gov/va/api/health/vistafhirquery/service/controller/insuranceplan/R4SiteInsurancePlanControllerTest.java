package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class R4SiteInsurancePlanControllerTest {
  @Mock CharonClient charon;

  MockWitnessProtection witnessProtection = new MockWitnessProtection();

  private R4SiteInsurancePlanController _controller() {
    return R4SiteInsurancePlanController.builder()
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
    var samples = InsurancePlanSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createInsurancePlanResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "123;355.3;ien1");
    _controller()
        .insurancePlanCreate(
            response, "123", InsurancePlanSamples.R4.create().insurancePlan("123", "ien1"));
    assertThat(captor.getValue().rpcRequest().api())
        .isEqualTo(LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi.CREATE);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeader(HttpHeaders.LOCATION))
        .isEqualTo("http://fugazi.com/site/123/r4/InsurancePlan/pub1");
  }

  @Test
  void readReturnsKnownResource() {
    var samples = InsurancePlanSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "s1;355.3;ien1");
    var actual = _controller().insurancePlanRead("s1", "pub1");
    var expected = InsurancePlanSamples.R4.create().insurancePlan("s1", "ien1");
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void readThrowsNotFoundForBadId() {
    witnessProtection.add("nope1", "nope1");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().insurancePlanRead("123", "nope1"));
  }

  @Test
  void readThrowsNotFoundForWrongFile() {
    witnessProtection.add("wrong1", "s1;wrong;ien1");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().insurancePlanRead("123", "wrong1"));
  }

  @Test
  void readThrowsNotFoundWhenNoResultsAreFound() {
    var results = LhsLighthouseRpcGatewayResponse.Results.builder().build();
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageSearch.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "s1;355.3;ien1");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().insurancePlanRead("123", "pub1"));
  }

  @Test
  void update() {
    var response = new MockHttpServletResponse();
    var samples = InsurancePlanSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createInsurancePlanResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "123;355.3;ien1");
    _controller()
        .insurancePlanUpdate(
            response, "123", "pub1", InsurancePlanSamples.R4.create().insurancePlan("123", "ien1"));
    assertThat(captor.getValue().rpcRequest().api())
        .isEqualTo(LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi.UPDATE);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void updateThrowsBadRequestPayloadForResourcesThatCannotBeProcessed() {
    var response = new MockHttpServletResponse();
    witnessProtection.add("pub1", "123;355.3;ien1");
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(
            () ->
                _controller()
                    .insurancePlanUpdate(
                        response,
                        "123",
                        "pub1",
                        InsurancePlanSamples.R4.create().insurancePlan("123", "ien1").type(null)));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"456"})
  void updateThrowsCannotUpdateResourceWithMismatchedIdsWhenUrlAndPayloadIdsDoNotMatch(
      String resourceId) {
    var response = new MockHttpServletResponse();
    witnessProtection.add("pub1", "123;355.3;ien1");
    assertThatExceptionOfType(ResourceExceptions.CannotUpdateResourceWithMismatchedIds.class)
        .isThrownBy(
            () ->
                _controller()
                    .insurancePlanUpdate(
                        response,
                        "123",
                        "pub1",
                        InsurancePlanSamples.R4.create().insurancePlan().id(resourceId)));
  }

  @Test
  void updateThrowsCannotUpdateUnknownResourceForUnknownResource() {
    var response = new MockHttpServletResponse();
    var samples = InsurancePlanSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.updateInsurancePlanWithNotExistsId();
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "123;355.3;ien1");
    assertThatExceptionOfType(ResourceExceptions.CannotUpdateUnknownResource.class)
        .isThrownBy(
            () ->
                _controller()
                    .insurancePlanUpdate(
                        response,
                        "123",
                        "pub1",
                        InsurancePlanSamples.R4.create().insurancePlan("123", "ien1")));
  }
}
