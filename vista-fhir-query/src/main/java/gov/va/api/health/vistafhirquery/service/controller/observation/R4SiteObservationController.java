package gov.va.api.health.vistafhirquery.service.controller.observation;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.vprGetPatientData;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.vprGetPatientDataResponse;

import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.vistafhirquery.service.api.R4SiteObservationApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.R4Controllers;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Labs;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Vitals;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

  private final VistaApiConfig vistaApiConfig;

  private final VitalVuidMapper vitalVuids;

  private final WitnessProtection witnessProtection;

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
        transformation(identifier.patientIdentifier(), null)
            .toResource()
            .apply(vprGetPatientDataResponse(response));
    return R4Controllers.verifyAndGetResult(fhir, id);
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

  private R4Transformation<VprGetPatientData.Response, Observation> transformation(
      String patientIdentifier, String codes) {
    return R4Transformation.<VprGetPatientData.Response, Observation>builder()
        .toResource(
            rpcResponse ->
                rpcResponse.resultsByStation().entrySet().parallelStream()
                    .filter(
                        entry ->
                            entry.getValue().vitalStream().anyMatch(Vitals.Vital::isNotEmpty)
                                || entry.getValue().labStream().anyMatch(Labs.Lab::isNotEmpty))
                    .flatMap(
                        entry ->
                            R4ObservationCollector.builder()
                                .patientIcn(patientIdentifier)
                                .resultsEntry(entry)
                                .vitalVuidMapper(vitalVuids)
                                .codes(codes)
                                .build()
                                .toFhir())
                    .collect(Collectors.toList()))
        .build();
  }

  private VprGetPatientData.Request vprRequest(String icn, Set<Domains> vprDomains, String id) {
    return VprGetPatientData.Request.builder()
        .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
        .dfn(VprGetPatientData.Request.PatientId.forIcn(icn))
        .type(vprDomains)
        .id(Optional.of(id))
        .build();
  }
}
