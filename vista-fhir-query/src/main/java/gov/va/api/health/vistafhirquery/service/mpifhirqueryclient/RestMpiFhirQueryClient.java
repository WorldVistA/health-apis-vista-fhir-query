package gov.va.api.health.vistafhirquery.service.mpifhirqueryclient;

import static gov.va.api.health.vistafhirquery.service.mpifhirqueryclient.MpiFhirQueryClientExceptions.MpiFhirQueryRequestFailed;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import gov.va.api.health.r4.api.resources.Endpoint;
import gov.va.api.health.r4.api.resources.Endpoint.Bundle;
import java.net.URI;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RestMpiFhirQueryClient implements MpiFhirQueryClient {

  private final RestTemplate restTemplate;
  private final MpiFhirQueryConfig config;
  private final String mfqBaseUrl;

  /** Constructor. */
  public RestMpiFhirQueryClient(
      @Autowired RestTemplate restTemplate,
      @Autowired MpiFhirQueryConfig config,
      @Value("${vista-fhir-query.mfq-base-url}") String mfqBaseUrl) {
    this.restTemplate = restTemplate;
    this.config = config;
    // Backwards compatibility
    this.mfqBaseUrl = isNotBlank(config.getBaseUrl()) ? config.getBaseUrl() : mfqBaseUrl;
  }

  @SneakyThrows
  private Bundle endpointsForPatient(String patient) {
    var url = mfqBaseUrl + "/r4/Endpoint?patient=" + patient;
    try {
      log.info("Invoking {}", url);
      var request = RequestEntity.get(URI.create(url)).headers(headers()).build();
      var response = restTemplate.exchange(request, Endpoint.Bundle.class);
      return response.getBody();
    } catch (HttpStatusCodeException e) {
      log.warn("Request failed {} with status {}", url, e.getStatusCode());
      throw new MpiFhirQueryRequestFailed(url, e);
    } catch (RestClientException e) {
      log.warn("Request failed {}", url);
      throw new MpiFhirQueryRequestFailed(url, e);
    }
  }

  /** Add client-key header if available. */
  private HttpHeaders headers() {
    var headers = new HttpHeaders();
    if (isNotBlank(config.getClientKey())) {
      headers.add("client-key", config.getClientKey());
    }
    return headers;
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
