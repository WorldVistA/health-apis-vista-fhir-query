package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.vprGetPatientData;
import static java.util.stream.Collectors.toSet;

import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.vistafhirquery.service.api.R4ConditionApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Request.PatientId;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@Builder
public class R4SiteConditionController implements R4ConditionApi {
  private final R4BundlerFactory bundlerFactory;

  private final VistaApiConfig vistaApiConfig;

  private final CharonClient charonClient;

  private Set<VprGetPatientData.Domains> categoryIs(String categoryCsv) {
    if (categoryCsv == null) {
      return Set.of(VprGetPatientData.Domains.problems, VprGetPatientData.Domains.visits);
    }
    var requestedCategories = categoryCsv.split(",", -1);
    return Arrays.stream(requestedCategories)
        .map(this::toVistaDomain)
        .filter(Objects::nonNull)
        .collect(toSet());
  }

  /** Search for Condition records by Patient. */
  @SneakyThrows
  @GetMapping(value = "/hcs/{site}/r4/Condition")
  public Condition.Bundle conditionSearch(
      HttpServletRequest request,
      @RequestParam(name = "category", required = false) String categoryCsv,
      @RequestParam(name = "clinical-status", required = false) String clinicalStatusCsv,
      @PathVariable(value = "site") String site,
      @RequestParam(name = "patient") String patientIcn,
      @RequestParam(
              value = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count) {
    Set<VprGetPatientData.Domains> categoryTypes = categoryIs(categoryCsv);
    if (categoryTypes.isEmpty()) {
      return emptyBundle(site, request);
    }
    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(PatientId.forIcn(patientIcn))
            .type(categoryTypes)
            .build();
    var charonResponse = charonClient.request(vprGetPatientData(site, rpcRequest));
    return bundlerFactory
        .forTransformation(transformation(patientIcn, site, clinicalStatusCsv, categoryCsv))
        .site(charonResponse.invocationResult().vista())
        .bundling(
            R4Bundling.newBundle(Condition.Bundle::new).newEntry(Condition.Entry::new).build())
        .resourceType("Condition")
        .request(request)
        .build()
        .apply(charonResponse.value());
  }

  private Condition.Bundle emptyBundle(String site, HttpServletRequest request) {
    var emptyVprResponse = VprGetPatientData.Response.Results.builder().build();
    return toBundle(site, request.getParameter("patient"), request).apply(emptyVprResponse);
  }

  private R4Bundler<
          VprGetPatientData.Response.Results, Condition, Condition.Entry, Condition.Bundle>
      toBundle(String site, String patientIcn, HttpServletRequest request) {
    return bundlerFactory
        .forTransformation(
            transformation(
                patientIcn,
                site,
                request.getParameter("clinical-status"),
                request.getParameter("category")))
        .site(site)
        .bundling(
            R4Bundling.newBundle(Condition.Bundle::new).newEntry(Condition.Entry::new).build())
        .resourceType("Condition")
        .request(request)
        .build();
  }

  private VprGetPatientData.Domains toVistaDomain(String maybeCategory) {
    if (maybeCategory == null) {
      return null;
    }
    switch (maybeCategory) {
      case "problem-list-item":
        return VprGetPatientData.Domains.problems;
      case "encounter-diagnosis":
        return VprGetPatientData.Domains.visits;
      default:
        return null;
    }
  }

  private R4Transformation<VprGetPatientData.Response.Results, Condition> transformation(
      String patientIdentifier, String site, String clinicalStatus, String category) {
    return R4Transformation.<VprGetPatientData.Response.Results, Condition>builder()
        .toResource(
            rpcResults ->
                R4ConditionCollector.builder()
                    .patientIcn(patientIdentifier)
                    .results(rpcResults)
                    .site(site)
                    .clinicalStatusCsv(clinicalStatus)
                    .categoryCsv(category)
                    .build()
                    .toFhir()
                    .collect(Collectors.toList()))
        .build();
  }
}
