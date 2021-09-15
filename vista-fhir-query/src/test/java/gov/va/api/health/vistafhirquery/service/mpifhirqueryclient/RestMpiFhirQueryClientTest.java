package gov.va.api.health.vistafhirquery.service.mpifhirqueryclient;

import static gov.va.api.health.vistafhirquery.service.controller.endpoint.EndpointSamples.R4.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Endpoint;
import gov.va.api.health.vistafhirquery.service.controller.endpoint.EndpointSamples;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class RestMpiFhirQueryClientTest {

  @Mock RestTemplate restTemplate;

  String mfqBaseUrl = "http://fake.com";

  @Test
  void stationIdsForPatient() {
    when(restTemplate.exchange(
            eq(mfqBaseUrl + "/r4/Endpoint?patient=1337"),
            eq(HttpMethod.GET),
            eq(null),
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
    assertThat(new RestMpiFhirQueryClient(restTemplate, mfqBaseUrl).stationIdsForPatient("1337"))
        .isEqualTo(Set.of("101", "104", "105", "106"));
  }
}
