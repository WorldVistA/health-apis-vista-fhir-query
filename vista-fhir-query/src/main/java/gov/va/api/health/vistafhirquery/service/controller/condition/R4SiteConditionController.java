package gov.va.api.health.vistafhirquery.service.controller.condition;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.vprGetPatientData;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.vistafhirquery.service.api.R4ConditionApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Controllers;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.health.vistafhirquery.service.util.CsvParameters;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Request.PatientId;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;
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

  private final WitnessProtection witnessProtection;

  private Set<VprGetPatientData.Domains> categoryIs(String categoryCsv) {
    if (isBlank(categoryCsv)) {
      return Set.of(VprGetPatientData.Domains.problems, VprGetPatientData.Domains.visits);
    }
    return CsvParameters.toSet(categoryCsv).stream()
        .map(this::toVistaDomain)
        .filter(Objects::nonNull)
        .collect(toSet());
  }

  /** Read by id. encounter-diagnosis Conditions have icd in id. */
  @Override
  @GetMapping(value = "/hcs/{site}/r4/Condition/{id}")
  public Condition conditionRead(@PathVariable("site") String site, @PathVariable("id") String id) {
    var identifier = parseIdOrDie(site, id);
    var request =
        vprRequest(
            identifier.vistaId().patientIdentifier(),
            Set.of(identifier.vistaId().vprRpcDomain()),
            identifier.vistaId().recordId());
    var response = charonClient.request(vprGetPatientData(site, request));
    var fhir =
        transformation(
                identifier.vistaId().patientIdentifier(),
                site,
                null,
                null,
                identifier.icdCode().orElse(null),
                null)
            .toResource()
            .apply(response.value());
    return R4Controllers.verifyAndGetResult(fhir, id);
  }

  /** Search for Condition records by Patient. */
  @SneakyThrows
  @GetMapping(value = "/hcs/{site}/r4/Condition")
  public Condition.Bundle conditionSearch(
      HttpServletRequest request,
      @PathVariable(value = "site") String site,
      @RequestParam(name = "patient", required = false) String patientIcn,
      @RequestParam(name = "_id", required = false) String id,
      @RequestParam(name = "identifier", required = false) String identifier,
      @RequestParam(name = "category", required = false) String categoryCsv,
      @RequestParam(name = "clinical-status", required = false) String clinicalStatusCsv,
      @RequestParam(name = "code", required = false) String code,
      @RequestParam(name = "onset-date", required = false) @Size(max = 2) String[] date,
      @RequestParam(
              value = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count) {
    if (allBlank(patientIcn, id, identifier, categoryCsv, code, date)) {
      throw ResourceExceptions.BadSearchParameters.because(
          "Condition search requires a patient, _id, or identifier query parameter.");
    }
    if (id != null || identifier != null) {
      return id == null
          ? searchByIdentifier(request, site, identifier)
          : searchByIdentifier(request, site, id);
    }
    Set<VprGetPatientData.Domains> categoryTypes = categoryIs(categoryCsv);
    if (categoryTypes.isEmpty()) {
      return emptyBundle(site, request);
    }
    var rpcRequest = vprRequest(patientIcn, categoryTypes, null);
    var charonResponse = charonClient.request(vprGetPatientData(site, rpcRequest));
    return bundlerFactory
        .forTransformation(
            transformation(patientIcn, site, clinicalStatusCsv, categoryCsv, code, date))
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
    return toBundle(site, request.getParameter("patient"), request, Optional.empty())
        .apply(emptyVprResponse);
  }

  private ConditionId parseIdOrDie(String urlSite, String id) {
    String identifier;
    ConditionId conditionId;
    try {
      identifier = witnessProtection.toPrivateId(id);
      conditionId = ConditionId.fromString(identifier);
    } catch (IdEncoder.BadId | IllegalArgumentException e) {
      throw ResourceExceptions.NotFound.because("Could not unpack id: " + id);
    }
    if (!urlSite.equals(conditionId.vistaId().siteId())) {
      throw new ResourceExceptions.NotFound(
          id + " does not exist at site " + urlSite + ": Site mismatch");
    }
    if (conditionId.vistaId().vprRpcDomain().equals(VprGetPatientData.Domains.visits)
        && conditionId.icdCode().isEmpty()) {
      throw new ResourceExceptions.NotFound(id + " is an invalid id for the visits domain");
    }
    return conditionId;
  }

  private Condition.Bundle searchByIdentifier(HttpServletRequest request, String site, String id) {
    ConditionId identifier;
    try {
      identifier = parseIdOrDie(site, id);
    } catch (ResourceExceptions.NotFound e) {
      return emptyBundle(site, request);
    }
    var vprRequest =
        vprRequest(
            identifier.vistaId().patientIdentifier(),
            Set.of(identifier.vistaId().vprRpcDomain()),
            identifier.vistaId().recordId());
    var response = charonClient.request(vprGetPatientData(site, vprRequest));
    return toBundle(site, identifier.vistaId().patientIdentifier(), request, identifier.icdCode())
        .apply(response.value());
  }

  private R4Bundler<
          VprGetPatientData.Response.Results, Condition, Condition.Entry, Condition.Bundle>
      toBundle(
          String site,
          String patientIcn,
          HttpServletRequest request,
          Optional<String> maybeIcdFromId) {
    return bundlerFactory
        .forTransformation(
            transformation(
                patientIcn,
                site,
                request.getParameter("clinical-status"),
                request.getParameter("category"),
                maybeIcdFromId.orElseGet(() -> request.getParameter("code")),
                request.getParameterValues("onset-date")))
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
      String patientIdentifier,
      String site,
      String clinicalStatus,
      String category,
      String code,
      String[] date) {
    return R4Transformation.<VprGetPatientData.Response.Results, Condition>builder()
        .toResource(
            rpcResults ->
                R4ConditionCollector.builder()
                    .patientIcn(patientIdentifier)
                    .results(rpcResults)
                    .site(site)
                    .clinicalStatusCsv(clinicalStatus)
                    .categoryCsv(category)
                    .code(code)
                    .date(date)
                    .build()
                    .toFhir()
                    .collect(Collectors.toList()))
        .build();
  }

  private VprGetPatientData.Request vprRequest(
      String icn, Set<VprGetPatientData.Domains> vprDomains, String id) {
    return VprGetPatientData.Request.builder()
        .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
        .dfn(PatientId.forIcn(icn))
        .type(vprDomains)
        .id(Optional.ofNullable(id))
        .build();
  }
}
