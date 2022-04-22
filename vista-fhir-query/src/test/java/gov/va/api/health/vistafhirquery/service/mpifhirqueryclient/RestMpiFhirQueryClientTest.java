package gov.va.api.health.vistafhirquery.service.mpifhirqueryclient;

import static gov.va.api.health.vistafhirquery.service.controller.endpoint.EndpointSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Endpoint;
import gov.va.api.health.vistafhirquery.service.controller.endpoint.EndpointSamples;
import gov.va.api.health.vistafhirquery.service.mpifhirqueryclient.MpiFhirQueryClientExceptions.MpiFhirQueryRequestFailed;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class RestMpiFhirQueryClientTest {

  @Mock RestTemplate restTemplate;

  String mfqBaseUrl = "http://fake.com";

  private RestMpiFhirQueryClient _client() {
    return new RestMpiFhirQueryClient(
        restTemplate,
        MpiFhirQueryConfig.builder().baseUrl(mfqBaseUrl).clientKey("foo").build(),
        null);
  }

  @Test
  void backwardsCompatible() {
    var client =
        new RestMpiFhirQueryClient(restTemplate, MpiFhirQueryConfig.builder().build(), mfqBaseUrl);
    when(restTemplate.exchange(
            eq(RequestEntity.get(URI.create(mfqBaseUrl + "/r4/Endpoint?patient=1337")).build()),
            eq(Endpoint.Bundle.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    EndpointSamples.R4.asBundle(
                        "http://fake.com",
                        List.of(EndpointSamples.R4.create().endpoint("101")),
                        1,
                        link(
                            BundleLink.LinkRelation.self,
                            "http://fake.com/r4/Endpoint?patient=1337")))));
    assertThat(client.stationIdsForPatient("1337")).isEqualTo(Set.of("101"));
  }

  @Test
  void clientWithNoClientKeyConfiguredDoesNotAddHeader() {
    var client =
        new RestMpiFhirQueryClient(
            restTemplate, MpiFhirQueryConfig.builder().baseUrl(mfqBaseUrl).build(), null);
    when(restTemplate.exchange(any(RequestEntity.class), eq(Endpoint.Bundle.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    EndpointSamples.R4.asBundle(
                        "http://fake.com",
                        List.of(EndpointSamples.R4.create().endpoint("101")),
                        1,
                        link(
                            BundleLink.LinkRelation.self,
                            "http://fake.com/r4/Endpoint?patient=1337")))));
    client.stationIdsForPatient("1337");
    verify(restTemplate)
        .exchange(
            eq(RequestEntity.get(URI.create(mfqBaseUrl + "/r4/Endpoint?patient=1337")).build()),
            eq(Endpoint.Bundle.class));
  }

  @Test
  void endpointThrowsExceptionWhenErrorOccurs() {
    when(restTemplate.exchange(
            eq(
                RequestEntity.get(URI.create(mfqBaseUrl + "/r4/Endpoint?patient=1337"))
                    .header("client-key", "foo")
                    .build()),
            eq(Endpoint.Bundle.class)))
        .thenThrow(new RestClientException("fugazi"));
    assertThatExceptionOfType(MpiFhirQueryRequestFailed.class)
        .isThrownBy(() -> _client().stationIdsForPatient("1337"));
  }

  @Test
  void endpointThrowsExceptionWhenStatusCodeExceptionOccurs() {
    when(restTemplate.exchange(
            eq(
                RequestEntity.get(URI.create(mfqBaseUrl + "/r4/Endpoint?patient=1337"))
                    .header("client-key", "foo")
                    .build()),
            eq(Endpoint.Bundle.class)))
        .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    assertThatExceptionOfType(MpiFhirQueryRequestFailed.class)
        .isThrownBy(() -> _client().stationIdsForPatient("1337"));
  }

  @Test
  void noStationIdsForPatient() {
    when(restTemplate.exchange(
            eq(
                RequestEntity.get(URI.create(mfqBaseUrl + "/r4/Endpoint?patient=1337"))
                    .header("client-key", "foo")
                    .build()),
            eq(Endpoint.Bundle.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    EndpointSamples.R4.asBundle(
                        "http://fake.com",
                        List.of(),
                        0,
                        link(
                            BundleLink.LinkRelation.self,
                            "http://fake.com/r4/Endpoint?patient=1337")))));
    assertThat(_client().stationIdsForPatient("1337")).isEqualTo(Set.of());
  }

  @Test
  void stationIdsForPatient() {
    when(restTemplate.exchange(
            eq(
                RequestEntity.get(URI.create(mfqBaseUrl + "/r4/Endpoint?patient=1337"))
                    .header("client-key", "foo")
                    .build()),
            eq(Endpoint.Bundle.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    EndpointSamples.R4.asBundle(
                        "http://fake.com",
                        List.of(
                            EndpointSamples.R4.create().endpoint("101"),
                            EndpointSamples.R4.create().endpoint("104"),
                            EndpointSamples.R4.create().endpoint("105"),
                            EndpointSamples.R4.create().endpoint("106")),
                        4,
                        link(
                            BundleLink.LinkRelation.self,
                            "http://fake.com/r4/Endpoint?patient=1337")))));
    assertThat(_client().stationIdsForPatient("1337"))
        .isEqualTo(Set.of("101", "104", "105", "106"));
  }
}
