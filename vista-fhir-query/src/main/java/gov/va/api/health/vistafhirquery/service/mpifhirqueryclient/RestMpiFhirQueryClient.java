package gov.va.api.health.vistafhirquery.service.mpifhirqueryclient;

import static gov.va.api.health.vistafhirquery.service.mpifhirqueryclient.MpiFhirQueryClientExceptions.MpiFhirQueryRequestFailed;
import static java.util.stream.Collectors.toSet;

import gov.va.api.health.r4.api.resources.Endpoint;
import gov.va.api.health.r4.api.resources.Endpoint.Bundle;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RestMpiFhirQueryClient implements MpiFhirQueryClient {

  private final RestTemplate restTemplate;
  private final String mfqBaseUrl;

  public RestMpiFhirQueryClient(
      @Autowired RestTemplate restTemplate,
      @Value("${vista-fhir-query.mfq-base-url}") String mfqBaseUrl) {
    this.restTemplate = restTemplate;
    this.mfqBaseUrl = mfqBaseUrl;
  }

  @SneakyThrows
  private Bundle endpointsForPatient(String patient) {
    var url = mfqBaseUrl + "/r4/Endpoint?patient=" + patient;
    try {
      log.info("Invoking {}", url);
      var response = restTemplate.exchange(url, HttpMethod.GET, null, Endpoint.Bundle.class);
      return response.getBody();
    } catch (HttpStatusCodeException e) {
      log.warn("Request failed {} with status {}", url, e.getStatusCode());
      throw new MpiFhirQueryRequestFailed(url, e);
    } catch (RestClientException e) {
      log.warn("Request failed {}", url);
      throw new MpiFhirQueryRequestFailed(url, e);
    }
  }

  @Override
  public Set<String> stationIdsForPatient(String patient) {
    var bundle = endpointsForPatient(patient);
    if (bundle == null || bundle.entry() == null || bundle.entry().isEmpty()) {
      log.info("No stations found found for patient");
      return Set.of();
    }
    return bundle.entry().stream().map(e -> e.resource().id()).collect(toSet());
  }
}
