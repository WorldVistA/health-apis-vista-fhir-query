package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class R4SiteOrganizationControllerTest {
  @Mock CharonClient charon;

  MockWitnessProtection witnessProtection = new MockWitnessProtection();

  private R4SiteOrganizationController _controller() {
    return R4SiteOrganizationController.builder()
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
  void readReturnsKnownResource() {
    var samples = OrganizationSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "s1;36;ien1");
    var actual = _controller().organizationRead("s1", "pub1");
    var expected = OrganizationSamples.R4.create().organization("s1", "ien1");
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void readThrowsNotFoundForBadId() {
    witnessProtection.add("nope1", "nope1");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().organizationRead("123", "nope1"));
  }

  @Test
  void readThrowsNotFoundForWrongFile() {
    witnessProtection.add("wrong1", "s1;wrong;ien1");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().organizationRead("123", "wrong1"));
  }

  @Test
  void readThrowsNotFoundWhenNoResultsAreFound() {
    var results = LhsLighthouseRpcGatewayResponse.Results.builder().build();
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageSearch.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "s1;36;ien1");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().organizationRead("123", "pub1"));
  }
}
