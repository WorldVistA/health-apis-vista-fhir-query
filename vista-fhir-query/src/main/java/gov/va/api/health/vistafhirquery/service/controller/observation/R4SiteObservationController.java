package gov.va.api.health.vistafhirquery.service.controller.observation;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.vprGetPatientData;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toLocalDateMacroString;
import static gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadSearchParameters;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.vistafhirquery.service.api.R4SiteObservationApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Controllers;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.health.vistafhirquery.service.util.CsvParameters;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Observation Profile using a VistA backend.
 *
 * @implSpec
 *     https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-observation-lab.html
 */
@Builder
@Validated
@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@RequestMapping(produces = {"application/json", "application/fhir+json"})
public class R4SiteObservationController implements R4SiteObservationApi {
  private final CharonClient charon;

  private final R4BundlerFactory bundlerFactory;

  private final VistaApiConfig vistaApiConfig;

  private final VitalVuidMapper vitalVuids;

  private final WitnessProtection witnessProtection;

  private Observation.Bundle emptyBundle(String site, HttpServletRequest request) {
    var emptyVprResponse = VprGetPatientData.Response.Results.builder().build();
    return toBundle(site, request.getParameter("patient"), request).apply(emptyVprResponse);
  }

  @Override
  @GetMapping(value = "/hcs/{site}/r4/Observation/{id}")
  public Observation observationRead(
      @PathVariable("site") String site, @PathVariable("id") String id) {
    var identifier = parseIdOrDie(site, id);
    var request =
        vprRequest(
            identifier.patientIdentifier(),
            Set.of(identifier.vprRpcDomain()),
            identifier.recordId());
    var response = charon.request(vprGetPatientData(site, request));
    var fhir =
        transformation(site, identifier.patientIdentifier(), null, null)
            .toResource()
            .apply(response.value());
    return R4Controllers.verifyAndGetResult(fhir, id);
  }

  @Override
  @GetMapping(value = "/hcs/{site}/r4/Observation")
  public Observation.Bundle observationSearch(
      HttpServletRequest request,
      @PathVariable("site") String site,
      @RequestParam(name = "patient", required = false) String patient,
      @RequestParam(name = "_id", required = false) String id,
      @RequestParam(name = "identifier", required = false) String identifier,
      @RequestParam(name = "category", required = false) String category,
      @RequestParam(name = "code", required = false) String code,
      @RequestParam(name = "date", required = false) @Size(max = 2) String[] date,
      @RequestParam(
              name = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count) {
    if (allBlank(patient, id, identifier, category, code, date)) {
      throw BadSearchParameters.because(
          "Observation search requires a patient, _id, or identifier query parameter.");
    }
    if (id != null || identifier != null) {
      return id == null
          ? searchByIdentifier(request, site, identifier)
          : searchByIdentifier(request, site, id);
    }
    var vprDomains = translateCategoriesToVprDomains(category);
    if (vprDomains.isEmpty()) {
      return emptyBundle(site, request);
    }
    var dates = DateSearchBoundaries.of(date);
    var vprRequest = vprRequest(patient, vprDomains, dates);
    var vprResponse = charon.request(vprGetPatientData(site, vprRequest));
    return toBundle(site, patient, request).apply(vprResponse.value());
  }

  private SegmentedVistaIdentifier parseIdOrDie(String urlSite, String id) {
    SegmentedVistaIdentifier identifier;
    try {
      identifier = SegmentedVistaIdentifier.unpack(witnessProtection.toPrivateId(id));
    } catch (IdEncoder.BadId | IllegalArgumentException e) {
      throw NotFound.because("Could not unpack id: " + id);
    }
    if (!urlSite.equals(identifier.siteId())) {
      throw new NotFound(id + " does not exist at site " + urlSite + ": Site mismatch");
    }
    return identifier;
  }

  private Observation.Bundle searchByIdentifier(
      HttpServletRequest request, String site, String id) {
    SegmentedVistaIdentifier identifier;
    try {
      identifier = parseIdOrDie(site, id);
    } catch (NotFound e) {
      return emptyBundle(site, request);
    }
    var vprRequest =
        vprRequest(
            identifier.patientIdentifier(),
            Set.of(identifier.vprRpcDomain()),
            identifier.recordId());
    var response = charon.request(vprGetPatientData(site, vprRequest));
    return toBundle(site, identifier.patientIdentifier(), request).apply(response.value());
  }

  private R4Bundler<
          VprGetPatientData.Response.Results, Observation, Observation.Entry, Observation.Bundle>
      toBundle(String site, String patient, HttpServletRequest request) {
    return bundlerFactory
        .forTransformation(
            transformation(
                site, patient, request.getParameter("code"), request.getParameter("category")))
        .site(site)
        .bundling(
            R4Bundling.newBundle(Observation.Bundle::new).newEntry(Observation.Entry::new).build())
        .resourceType(Observation.class.getSimpleName())
        .request(request)
        .build();
  }

  private R4Transformation<VprGetPatientData.Response.Results, Observation> transformation(
      String site, String patientIdentifier, String codes, String category) {
    return R4Transformation.<VprGetPatientData.Response.Results, Observation>builder()
        .toResource(
            results ->
                R4ObservationCollector.builder()
                    .site(site)
                    .patientIcn(patientIdentifier)
                    .results(results)
                    .vitalVuidMapper(vitalVuids)
                    .codes(codes)
                    .categoryCsv(category)
                    .build()
                    .toFhir()
                    .collect(toList()))
        .build();
  }

  private Set<Domains> translateCategoriesToVprDomains(String categoryCsv) {
    if (isBlank(categoryCsv)) {
      return Set.of(Domains.labs, Domains.vitals);
    }
    return CsvParameters.toStream(categoryCsv)
        .map(this::vprDomainForCategory)
        .filter(Objects::nonNull)
        .collect(toSet());
  }

  private Domains vprDomainForCategory(String maybeCategory) {
    if (maybeCategory == null) {
      return null;
    }
    switch (maybeCategory) {
      case "vital-signs":
        return Domains.vitals;
      case "laboratory":
        return Domains.labs;
      default:
        return null;
    }
  }

  private VprGetPatientData.Request vprRequest(String icn, Set<Domains> vprDomains, String id) {
    return vprRequest(icn, vprDomains, id, DateSearchBoundaries.of(null));
  }

  private VprGetPatientData.Request vprRequest(
      String icn, Set<Domains> vprDomains, DateSearchBoundaries dates) {
    return vprRequest(icn, vprDomains, null, dates);
  }

  private VprGetPatientData.Request vprRequest(
      String icn, Set<Domains> vprDomains, String id, DateSearchBoundaries dates) {
    return VprGetPatientData.Request.builder()
        .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
        .dfn(VprGetPatientData.Request.PatientId.forIcn(icn))
        .type(vprDomains)
        .id(Optional.ofNullable(id))
        .start(toLocalDateMacroString(dates.start()))
        .stop(toLocalDateMacroString(dates.stop()))
        .build();
  }
}
