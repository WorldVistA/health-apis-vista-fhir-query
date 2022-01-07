package gov.va.api.health.vistafhirquery.service.controller.observation;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.answerFor;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonTestSupport.requestCaptor;
import static gov.va.api.health.vistafhirquery.service.controller.MockRequests.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import java.util.List;
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
}
