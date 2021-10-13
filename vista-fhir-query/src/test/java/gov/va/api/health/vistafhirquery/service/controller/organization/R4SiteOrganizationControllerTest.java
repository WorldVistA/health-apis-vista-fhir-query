package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageSamples.R4.link;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.MismatchedFileCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayListManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class R4SiteOrganizationControllerTest {
  @Mock CharonClient charon;

  MockWitnessProtection witnessProtection = new MockWitnessProtection();

  private R4SiteOrganizationController _controller() {
    return R4SiteOrganizationController.builder()
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
    var samples = OrganizationSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createOrganizationResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "123;36;ien1");
    _controller()
        .organizationCreate(
            response, "123", OrganizationSamples.R4.create().organization("123", "ien1"));
    assertThat(captor.getValue().rpcRequest().api())
        .isEqualTo(LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi.CREATE);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeader("Location"))
        .isEqualTo("http://fugazi.com/site/123/r4/Organization/pub1");
  }

  @Test
  void readReturnsInsuranceCompanyResource() {
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
  void readReturnsPayerResource() {
    var samples = OrganizationPayerSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayListManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "s1;365.12;ien1");
    var actual = _controller().organizationRead("s1", "pub1");
    var expected = OrganizationPayerSamples.R4.create().organization("s1", "ien1");
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void readThrowsForWrongFile() {
    witnessProtection.add("wrong1", "s1;wrong;ien1");
    assertThatExceptionOfType(MismatchedFileCoordinates.class)
        .isThrownBy(() -> _controller().organizationRead("123", "wrong1"));
  }

  @Test
  void readThrowsNotFoundForBadId() {
    witnessProtection.add("nope1", "nope1");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().organizationRead("123", "nope1"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"s1;36;ien1", "s1;365.12;ien1"})
  void readThrowsNotFoundWhenNoResultsAreFound(String privateId) {
    var results = LhsLighthouseRpcGatewayResponse.Results.builder().build();
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageSearch.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", privateId);
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().organizationRead("123", "pub1"));
  }

  @Test
  void searchForInsTypeReturnsInsCompanyResults() {
    var httpRequest = requestFromUri("?_count=10&type=ins");
    var samples = OrganizationSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayListManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    var actual = _controller().organizationSearch(httpRequest, "123", "ins", 10);
    var expected =
        OrganizationSamples.R4.asBundle(
            "http://fugazi.com/site/123/r4",
            List.of(OrganizationSamples.R4.create().organization("123", "ien1")),
            1,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/site/123/r4/Organization",
                "_count=10&type=ins"));
    assertThat(captor.getValue().rpcRequest().file()).isEqualTo(InsuranceCompany.FILE_NUMBER);
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = "NOPE")
  void searchForNullOrUnknownTypeReturnsNoResults(String type) {
    var typeParam = type == null ? "" : "&type=" + type;
    var httpRequest = requestFromUri("?_count=10" + typeParam);
    var actual = _controller().organizationSearch(httpRequest, "123", "NOPE", 10);
    var expected =
        OrganizationSamples.R4.asBundle(
            "http://fugazi.com/site/123/r4",
            emptyList(),
            0,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/site/123/r4/Organization",
                "_count=10" + typeParam));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchForPayTypeReturnsPayerResults() {
    var httpRequest = requestFromUri("?_count=10&type=pay");
    var samples = OrganizationPayerSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayListManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    var actual = _controller().organizationSearch(httpRequest, "123", "pay", 10);
    var expected =
        OrganizationSamples.R4.asBundle(
            "http://fugazi.com/site/123/r4",
            List.of(OrganizationPayerSamples.R4.create().organization("123", "ien1")),
            1,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/site/123/r4/Organization",
                "_count=10&type=pay"));
    assertThat(captor.getValue().rpcRequest().file()).isEqualTo(Payer.FILE_NUMBER);
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void update() {
    var response = new MockHttpServletResponse();
    var samples = OrganizationSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createOrganizationResults("ien1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("pub1", "123;36;ien1");
    _controller()
        .organizationUpdate(
            response, "123", "pub1", OrganizationSamples.R4.create().organization("123", "ien1"));
    assertThat(captor.getValue().rpcRequest().api())
        .isEqualTo(LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi.UPDATE);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void updateUnsupportedFileThrows() {
    var response = new MockHttpServletResponse();
    witnessProtection.add("pub1", "123;365.12;ien1");
    assertThatExceptionOfType(MismatchedFileCoordinates.class)
        .isThrownBy(
            () ->
                _controller()
                    .organizationUpdate(
                        response,
                        "123",
                        "pub1",
                        OrganizationSamples.R4.create().organization("123", "ien1")));
  }
}
