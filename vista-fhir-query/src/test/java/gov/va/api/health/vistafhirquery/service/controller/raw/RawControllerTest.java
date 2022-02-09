package gov.va.api.health.vistafhirquery.service.controller.raw;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageSamples;
import gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse.CoverageEligibilityResponseSamples;
import gov.va.api.health.vistafhirquery.service.controller.organization.OrganizationSamples.VistaLhsLighthouseRpcGateway;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageEligibilityResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RawControllerTest {
  @Mock CharonClient charon;

  MockWitnessProtection wp = new MockWitnessProtection();

  RawController _controller() {
    return RawController.builder().charon(charon).witnessProtection(wp).build();
  }

  private RpcInvocationResultV1 _invocationResult(Object value) {
    return RpcInvocationResultV1.builder()
        .vista("123")
        .timezone("UTC")
        .response(json(value))
        .build();
  }

  @Test
  void coverageBySiteAndIcnReturnsWhateverRpcResponseIsFound() {
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    assertThat(_controller().coverageBySiteAndIcn("666", "itME"))
        .isEqualTo(_invocationResult(results));
  }

  @Test
  void coverageEligibilityResponseByIdReturnsGatewayResponse() {
    var samples = CoverageEligibilityResponseSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ien2");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageEligibilityResponse.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    wp.add("pub2", "666+355.32+ien2");
    var actualLhsResponse = _controller().coverageEligibilityResponseById("pub2");
    assertThat(actualLhsResponse).isEqualTo(_invocationResult(results));
  }

  @Test
  void organizationByIdReturnsGatewayResponse() {
    var samples = VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    wp.add("pub1", "site1;36;ien1");
    var actualLhsResponse = _controller().organizationById("pub1");
    assertThat(actualLhsResponse).isEqualTo(_invocationResult(results));
  }

  @Test
  void rawEndpointThrowsCharonsException() {
    var samples = VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    when(charon.request(captor.capture())).thenThrow(IllegalArgumentException.class);
    wp.add("pub1", "site1;36;ien1");
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> _controller().organizationById("pub1"));
  }
}
