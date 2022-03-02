package gov.va.api.health.vistafhirquery.service.controller.endpoint;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import gov.va.api.health.r4.api.resources.Endpoint;
import gov.va.api.health.r4.api.resources.Endpoint.EndpointStatus;
import gov.va.api.health.vistafhirquery.service.api.R4EndpointApi;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.mpifhirqueryclient.MpiFhirQueryClient;
import gov.va.api.lighthouse.charon.api.v1.RpcPrincipalLookupV1;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = {"/r4/Endpoint"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@Slf4j
public class R4EndpointController implements R4EndpointApi {

  private final R4BundlerFactory bundlerFactory;

  private final LinkProperties linkProperties;

  private final RpcPrincipalLookupV1 rpcPrincipalLookup;

  private final MpiFhirQueryClient mpiFhirQueryClient;

  private Set<String> allStations() {
    return rpcPrincipalLookup.findAllEntries().keySet();
  }

  @Override
  @GetMapping(value = "/{site}")
  public Endpoint endpointRead(@PathVariable("site") String site) {
    if (isKnownSite(site)) {
      return R4EndpointTransformer.builder()
          .site(site)
          .linkProperties(linkProperties)
          .build()
          .toFhir();
    }
    throw new ResourceExceptions.NotFound("Unknown site: " + site);
  }

  /** Return a bundle of all endpoints. */
  @Override
  @GetMapping
  public Endpoint.Bundle endpointSearch(
      HttpServletRequest request,
      @Min(0)
          @RequestParam(
              value = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count,
      @RequestParam(name = "patient", required = false) String patient,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(name = "tag", required = false) String tag) {
    if (!isSupportedStatus(status)) {
      return toBundle(request).apply(emptySet());
    }
    Set<String> stations;
    if (isNotBlank(tag)) {
      stations = stationsByTag(tag);
      log.info("Stations tagged({}): {}", tag, sorted(stations));
    } else {
      stations = allStations();
      log.info("All stations: {}", sorted(stations));
    }
    if (isNotBlank(patient)) {
      Set<String> stationsForPatient = patientStations(patient);
      log.info("Patient stations:   {}", sorted(stationsForPatient));
      stations.retainAll(stationsForPatient);
    }
    return toBundle(request).apply(stations);
  }

  private boolean isKnownSite(String site) {
    return rpcPrincipalLookup.findAllEntries().containsKey(site);
  }

  private boolean isSupportedStatus(String status) {
    return status == null || EndpointStatus.active.toString().equals(status);
  }

  @SneakyThrows
  private Set<String> patientStations(String patient) {
    return mpiFhirQueryClient.stationIdsForPatient(patient);
  }

  private List<String> sorted(Collection<String> values) {
    var sorted = new ArrayList<>(values);
    Collections.sort(sorted);
    return sorted;
  }

  private Set<String> stationsByTag(String tag) {
    return rpcPrincipalLookup.findByTag(tag).keySet();
  }

  private R4Bundler<Set<String>, Endpoint, Endpoint.Entry, Endpoint.Bundle> toBundle(
      HttpServletRequest request) {
    return bundlerFactory
        .forTransformation(transformation())
        .withoutSite()
        .bundling(R4Bundling.newBundle(Endpoint.Bundle::new).newEntry(Endpoint.Entry::new).build())
        .resourceType("Endpoint")
        .request(request)
        .build();
  }

  private R4Transformation<Set<String>, Endpoint> transformation() {
    return R4Transformation.<Set<String>, Endpoint>builder()
        .toResource(
            station ->
                station.stream()
                    .map(
                        rpcResults ->
                            R4EndpointTransformer.builder()
                                .site(rpcResults)
                                .linkProperties(linkProperties)
                                .build()
                                .toFhir())
                    .collect(toList()))
        .build();
  }
}
