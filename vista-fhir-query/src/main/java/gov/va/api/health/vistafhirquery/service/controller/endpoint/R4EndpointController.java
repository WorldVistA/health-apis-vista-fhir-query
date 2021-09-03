package gov.va.api.health.vistafhirquery.service.controller.endpoint;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.resources.Endpoint;
import gov.va.api.health.r4.api.resources.Endpoint.EndpointStatus;
import gov.va.api.health.vistafhirquery.service.api.R4EndpointApi;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.lighthouse.charon.api.RpcPrincipalLookup;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = {"/r4/Endpoint"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
public class R4EndpointController implements R4EndpointApi {

  private final R4BundlerFactory bundlerFactory;

  private final LinkProperties linkProperties;

  private RpcPrincipalLookup rpcPrincipalLookup;

  /** Return a bundle of all endpoints. */
  @Override
  @GetMapping
  public Endpoint.Bundle endpointSearch(
      HttpServletRequest request,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "_count", required = false, defaultValue = "100") int count) {
    if (!isSupportedStatus(status)) {
      return toBundle(request).apply(emptySet());
    }
    Set<String> stations = stations("LHS LIGHTHOUSE RPC GATEWAY");
    return toBundle(request).apply(stations);
  }

  private boolean isSupportedStatus(String status) {
    return status == null || EndpointStatus.active.toString().equals(status);
  }

  private Set<String> stations(String rpcName) {
    return rpcPrincipalLookup.findByName(rpcName).keySet();
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
