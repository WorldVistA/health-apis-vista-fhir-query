package gov.va.api.health.vistafhirquery.service.controller.observation;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.requestFromUri;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class R4SiteObservationControllerTest {
  private static VitalVuidMapper vuids;

  @Mock CharonClient charon;

  MockWitnessProtection witnessProtection = new MockWitnessProtection();

  @BeforeAll
  static void _init() {
    VitalVuidMappingRepository repository = mock(VitalVuidMappingRepository.class);
    when(repository.findByCodingSystemId(eq((short) 11)))
        .thenReturn(ObservationVitalSamples.Datamart.create().mappingEntities());
    vuids = new VitalVuidMapper(repository);
  }

  private R4SiteObservationController _controller() {
    return R4SiteObservationController.builder()
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
        .vistaApiConfig(mock(VistaApiConfig.class))
        .vitalVuids(vuids)
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
  void readIdSiteMismatchThrowsNotFound() {
    witnessProtection.add("obs1", "sNp1+123+V456");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().observationRead("456", "obs1"));
  }

  @Test
  void readLabs() {
    var vistaLabSamples = ObservationLabSamples.Vista.create();
    var results = vistaLabSamples.results(vistaLabSamples.lab("456"));
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("obs1", "sNp1+123+L456");
    var actual = _controller().observationRead("123", "obs1");
    assertThat(json(actual))
        .isEqualTo(json(ObservationLabSamples.Fhir.create().observation("123", "sNp1+123+L456")));
  }

  @Test
  void readNoResultsThrowsNotFound() {
    var samples = ObservationVitalSamples.Vista.create();
    var results = samples.results();
    results.vitals().vitalResults(List.of());
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("obs1", "sNp1+123+V456");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().observationRead("123", "obs1"));
  }

  @Test
  void readUnknownIdThrowsNotFound() {
    witnessProtection.add("obs1", "sNp1+123+V456");
    assertThatExceptionOfType(NotFound.class)
        .isThrownBy(() -> _controller().observationRead("123", "obs2"));
  }

  @Test
  void readVitals() {
    var samples = ObservationVitalSamples.Vista.create();
    var results = samples.results();
    results.vitals().vitalResults().get(0).measurements(List.of(samples.weight("456")));
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("obs1", "sNp1+123+V456");
    var actual = _controller().observationRead("123", "obs1");
    assertThat(json(actual))
        .isEqualTo(json(ObservationVitalSamples.Fhir.create().weight("123", "sNp1+123+V456")));
  }

  @Test
  void searchById() {
    HttpServletRequest request = requestFromUri("?_id=obs1");
    var samples = ObservationVitalSamples.Vista.create();
    var results = samples.results();
    results.vitals().vitalResults().get(0).measurements(List.of(samples.bloodPressure("456")));
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("obs1", "sNp1+123+V456");
    var actual =
        _controller().observationSearch(request, "123", null, "obs1", null, null, null, null, 15);
    var expected =
        ObservationVitalSamples.Fhir.asBundle(
            "http://fugazi.com/hcs/123/r4",
            List.of(ObservationVitalSamples.Fhir.create().bloodPressure("123", "sNp1+123+V456")),
            1,
            ObservationVitalSamples.Fhir.link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/123/r4/Observation",
                "_id=obs1"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchByIdentifier() {
    HttpServletRequest request = requestFromUri("?identifier=obs1");
    var samples = ObservationVitalSamples.Vista.create();
    var results = samples.results();
    results.vitals().vitalResults().get(0).measurements(List.of(samples.bloodPressure("456")));
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    witnessProtection.add("obs1", "sNp1+123+V456");
    var actual =
        _controller().observationSearch(request, "123", null, null, "obs1", null, null, null, 15);
    var expected =
        ObservationVitalSamples.Fhir.asBundle(
            "http://fugazi.com/hcs/123/r4",
            List.of(ObservationVitalSamples.Fhir.create().bloodPressure("123", "sNp1+123+V456")),
            1,
            ObservationVitalSamples.Fhir.link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/123/r4/Observation",
                "identifier=obs1"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchByPatient() {
    HttpServletRequest request = requestFromUri("?patient=p1");
    var vitalSamples = ObservationVitalSamples.Vista.create();
    var labSamples = ObservationLabSamples.Vista.create();
    var results = vitalSamples.results();
    results
        .vitals()
        .vitalResults()
        .get(0)
        .measurements(List.of(vitalSamples.weight("789"), vitalSamples.bloodPressure("456")));
    results.labs(labSamples.results().labs());
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    var actual =
        _controller().observationSearch(request, "123", "p1", null, null, null, null, null, 15);
    var expected =
        ObservationVitalSamples.Fhir.asBundle(
            "http://fugazi.com/hcs/123/r4",
            List.of(
                ObservationVitalSamples.Fhir.create().weight("123", "sNp1+123+V789"),
                ObservationVitalSamples.Fhir.create().bloodPressure("123", "sNp1+123+V456"),
                ObservationLabSamples.Fhir.create()
                    .observation("123", "sNp1+123+LCH;6899283.889996;741")),
            3,
            ObservationVitalSamples.Fhir.link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/123/r4/Observation",
                "patient=p1"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchByPatientAndCategoryIsLabs() {
    HttpServletRequest request = requestFromUri("?patient=p1&category=laboratory");
    var vitalSamples = ObservationVitalSamples.Vista.create();
    var labSamples = ObservationLabSamples.Vista.create();
    var results = vitalSamples.results();
    results
        .vitals()
        .vitalResults()
        .get(0)
        .measurements(List.of(vitalSamples.weight("789"), vitalSamples.bloodPressure("456")));
    results.labs(labSamples.results().labs());
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    var actual =
        _controller()
            .observationSearch(request, "123", "p1", null, null, "laboratory", null, null, 15);
    var expected =
        ObservationVitalSamples.Fhir.asBundle(
            "http://fugazi.com/hcs/123/r4",
            List.of(
                ObservationLabSamples.Fhir.create()
                    .observation("123", "sNp1+123+LCH;6899283.889996;741")),
            1,
            ObservationVitalSamples.Fhir.link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/123/r4/Observation",
                "patient=p1&category=laboratory"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchByPatientAndCategoryIsVitals() {
    HttpServletRequest request = requestFromUri("?patient=p1&category=vital-signs");
    var vitalSamples = ObservationVitalSamples.Vista.create();
    var labSamples = ObservationLabSamples.Vista.create();
    var results = vitalSamples.results();
    results
        .vitals()
        .vitalResults()
        .get(0)
        .measurements(List.of(vitalSamples.weight("789"), vitalSamples.bloodPressure("456")));
    results.labs(labSamples.results().labs());
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    var actual =
        _controller()
            .observationSearch(request, "123", "p1", null, null, "vital-signs", null, null, 15);
    var expected =
        ObservationVitalSamples.Fhir.asBundle(
            "http://fugazi.com/hcs/123/r4",
            List.of(
                ObservationVitalSamples.Fhir.create().weight("123", "sNp1+123+V789"),
                ObservationVitalSamples.Fhir.create().bloodPressure("123", "sNp1+123+V456")),
            2,
            ObservationVitalSamples.Fhir.link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/123/r4/Observation",
                "patient=p1&category=vital-signs"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchByPatientAndCode() {
    HttpServletRequest request = requestFromUri("?patient=p1&category=vital-signs");
    var vitalSamples = ObservationVitalSamples.Vista.create();
    var labSamples = ObservationLabSamples.Vista.create();
    var results = vitalSamples.results();
    results
        .vitals()
        .vitalResults()
        .get(0)
        .measurements(List.of(vitalSamples.weight("789"), vitalSamples.bloodPressure("456")));
    results.labs(labSamples.results().labs());
    var captor = requestCaptor(VprGetPatientData.Request.class);
    var answer =
        answerFor(captor).value(results).invocationResult(_invocationResult(results)).build();
    when(charon.request(captor.capture())).thenAnswer(answer);
    var actual =
        _controller()
            .observationSearch(request, "123", "p1", null, null, "vital-signs", null, null, 15);
    var expected =
        ObservationVitalSamples.Fhir.asBundle(
            "http://fugazi.com/hcs/123/r4",
            List.of(
                ObservationVitalSamples.Fhir.create().weight("123", "sNp1+123+V789"),
                ObservationVitalSamples.Fhir.create().bloodPressure("123", "sNp1+123+V456")),
            2,
            ObservationVitalSamples.Fhir.link(
                BundleLink.LinkRelation.self,
                "http://fugazi.com/hcs/123/r4/Observation",
                "patient=p1&category=vital-signs"));
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchByPatientAndUnknownCategoryReturnsEmptyBundle() {
    HttpServletRequest request = requestFromUri("?patient=p1&category=whodis");
    var actual =
        _controller()
            .observationSearch(request, "123", "p1", null, null, "who-dis", null, null, 15);
    var expected =
        ObservationVitalSamples.Fhir.asBundle(
            "http://fugazi.com/hcs/123/r4/Observation",
            List.of(),
            0,
            BundleLink.builder()
                .relation(BundleLink.LinkRelation.self)
                .url("http://fugazi.com/hcs/123/r4/Observation?patient=p1&category=whodis")
                .build());
    assertThat(json(actual)).isEqualTo(json(expected));
  }

  @Test
  void searchWithNoParametersSpecifiedIsBadRequest() {
    HttpServletRequest request = requestFromUri("");
    assertThatExceptionOfType(ResourceExceptions.BadSearchParameters.class)
        .isThrownBy(
            () ->
                _controller()
                    .observationSearch(request, null, null, null, null, null, null, null, 15));
  }
}
