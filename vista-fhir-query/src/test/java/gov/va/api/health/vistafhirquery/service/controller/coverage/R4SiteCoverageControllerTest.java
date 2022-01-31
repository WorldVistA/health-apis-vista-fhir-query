package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToUpdateUnknownRecord;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.CannotUpdateResourceWithMismatchedIds;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
                        .publicR4BasePath("hcs/{site}/r4")
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
    var results = samples.createInsuranceTypeResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    when(witnessProtection.toPublicId(Coverage.class, "p1+123+2.312+ip1")).thenReturn("public-ip1");
    _controller()
        .coverageCreate(
            response, "123", false, CoverageSamples.R4.create().coverage("123", "not-used", "p1"));
    assertThat(captor.getValue().rpcRequest().api()).isEqualTo(CoverageWriteApi.CREATE);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeader("Location"))
        .isEqualTo("http://fugazi.com/hcs/123/r4/Coverage/public-ip1");
  }

  @Test
  void read() {
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.getsManifestResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    when(witnessProtection.toPatientTypeCoordinatesOrDie(
            "pubCover1", Coverage.class, InsuranceType.FILE_NUMBER))
        .thenReturn(
            PatientTypeCoordinates.builder()
                .icn("p1")
                .site("123")
                .file(InsuranceType.FILE_NUMBER)
                .ien("ip1")
                .build());
    var actual = _controller().coverageRead("123", "pubCover1", false);
    var expected = CoverageSamples.R4.create().coverage("123", "ip1", "p1");
    assertThat(json(actual)).isEqualTo(json(expected));
    var request = captor.getValue();
    assertThat(request.vista()).isEqualTo("123");
  }

  @Test
  void readFromBuffer() {
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createInsuranceBufferResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayGetsManifest.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    when(witnessProtection.toPatientTypeCoordinatesOrDie(
            "pubCover1", Coverage.class, InsuranceVerificationProcessor.FILE_NUMBER))
        .thenReturn(
            PatientTypeCoordinates.builder()
                .icn("p1")
                .site("123")
                .file(InsuranceVerificationProcessor.FILE_NUMBER)
                .ien("ip1")
                .build());
    var actual = _controller().coverageRead("123", "pubCover1", true);
    var expected = CoverageSamples.R4.create().coverageInsuranceBufferRead("p1", "123", "ip1");
    CoverageSamples.R4.cleanUpContainedReferencesForComparison(actual);
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
            "http://fugazi.com/hcs/123/r4",
            List.of(CoverageSamples.R4.create().coverage("123", "1,8,", "p1")),
            1,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/123/r4/Coverage",
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
            "http://fugazi.com/hcs/123/r4",
            List.of(),
            0,
            link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/123/r4/Coverage",
                "page=1&_count=10&patient=p1"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void update() {
    var response = new MockHttpServletResponse();
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.createInsuranceTypeResults("ip1");
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    when(witnessProtection.toPatientTypeCoordinatesOrDie(
            "public-c1", Coverage.class, InsuranceType.FILE_NUMBER))
        .thenReturn(
            PatientTypeCoordinates.builder()
                .site("123")
                .file(InsuranceType.FILE_NUMBER)
                .ien("ip1")
                .icn("p1")
                .build());
    _controller()
        .coverageUpdate(
            response, "123", "public-c1", CoverageSamples.R4.create().coverage("123", "ip1", "p1"));
    assertThat(captor.getValue().rpcRequest().api()).isEqualTo(CoverageWriteApi.UPDATE);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"456"})
  void updateThrowsCannotUpdateResourceWithMismatchedIdsWhenUrlAndPayloadIdsDoNotMatch(
      String resourceId) {
    var response = new MockHttpServletResponse();
    when(witnessProtection.toPatientTypeCoordinatesOrDie(
            "public-c1", Coverage.class, InsuranceType.FILE_NUMBER))
        .thenReturn(
            PatientTypeCoordinates.builder()
                .site("123")
                .file(InsuranceType.FILE_NUMBER)
                .ien("ip1")
                .icn("p1")
                .build());
    assertThatExceptionOfType(CannotUpdateResourceWithMismatchedIds.class)
        .isThrownBy(
            () ->
                _controller()
                    .coverageUpdate(
                        response,
                        "123",
                        "public-c1",
                        CoverageSamples.R4.create().coverage().id(resourceId)));
  }

  @Test
  void updateThrowsCannotUpdateUnknownResourceForUnknownResource() {
    var response = new MockHttpServletResponse();
    var samples = CoverageSamples.VistaLhsLighthouseRpcGateway.create();
    var results = samples.updateCoverageWithNotExistsId();
    var captor = requestCaptor(LhsLighthouseRpcGatewayCoverageWrite.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    when(witnessProtection.toPatientTypeCoordinatesOrDie(
            "public-c1", Coverage.class, InsuranceType.FILE_NUMBER))
        .thenReturn(
            PatientTypeCoordinates.builder()
                .site("123")
                .file(InsuranceType.FILE_NUMBER)
                .ien("ip1")
                .icn("p1")
                .build());
    assertThatExceptionOfType(AttemptToUpdateUnknownRecord.class)
        .isThrownBy(
            () ->
                _controller()
                    .coverageUpdate(
                        response,
                        "123",
                        "public-c1",
                        CoverageSamples.R4.create().coverage("123", "ip1", "p1")));
  }

  @Test
  void updateThrowsForResourcesThatCannotBeProcessed() {
    var response = new MockHttpServletResponse();
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _controller()
                    .coverageUpdate(
                        response,
                        "123",
                        "public-c1",
                        CoverageSamples.R4
                            .create()
                            .coverage("123", "ip1", "p1")
                            .beneficiary(null)));
  }

  @Test
  void updateThrowsWhenPatientMismatch() {
    var sample = CoverageSamples.R4.create().coverage("123", "456", "p1");
    when(witnessProtection.toPatientTypeCoordinatesOrDie(
            "public-c1", Coverage.class, InsuranceType.FILE_NUMBER))
        .thenReturn(
            PatientTypeCoordinates.builder()
                .site("123")
                .file(InsuranceType.FILE_NUMBER)
                .ien("456")
                .icn("p1")
                .build());
    sample.beneficiary().reference("Patient/p2");
    assertThatExceptionOfType(ResourceExceptions.ExpectationFailed.class)
        .isThrownBy(
            () ->
                _controller()
                    .coverageUpdate(mock(HttpServletResponse.class), "123", "public-c1", sample));
  }

  @Test
  void updateThrowsWhenSiteMismatch() {
    var sample = CoverageSamples.R4.create().coverage("123", "456", "p1");
    when(witnessProtection.toPatientTypeCoordinatesOrDie(
            "public-c1", Coverage.class, InsuranceType.FILE_NUMBER))
        .thenReturn(
            PatientTypeCoordinates.builder()
                .site("123")
                .file(InsuranceType.FILE_NUMBER)
                .ien("456")
                .icn("p1")
                .build());
    assertThatExceptionOfType(ResourceExceptions.ExpectationFailed.class)
        .isThrownBy(
            () ->
                _controller()
                    .coverageUpdate(mock(HttpServletResponse.class), "321", "public-c1", sample));
  }
}
