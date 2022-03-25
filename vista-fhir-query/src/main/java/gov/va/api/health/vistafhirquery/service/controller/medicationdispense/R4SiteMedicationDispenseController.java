package gov.va.api.health.vistafhirquery.service.controller.medicationdispense;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.vprGetPatientData;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static gov.va.api.health.vistafhirquery.service.controller.medicationdispense.R4MedicationDispenseTransformer.acceptAll;
import static gov.va.api.health.vistafhirquery.service.controller.medicationdispense.R4MedicationDispenseTransformer.acceptOnlyWithFillDateEqualTo;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.r4.api.resources.MedicationDispense;
import gov.va.api.health.vistafhirquery.service.api.R4MedicationDispenseApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.Meds.Med.Fill;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Domains;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Request.PatientId;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Response.Results;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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

/**
 * Request Mappings for Medication Dispense Profile using a VistA backend.
 *
 * @implSpec https://hl7.org/fhir/R4/medicationdispense.html
 */
@Validated
@RestController
@RequestMapping(produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@Builder
public class R4SiteMedicationDispenseController implements R4MedicationDispenseApi {

  private final R4BundlerFactory bundlerFactory;

  private final CharonClient charonClient;

  private final VistaApiConfig vistaApiConfig;

  private final WitnessProtection witnessProtection;

  /** Read by id. */
  @SneakyThrows
  @GetMapping(value = "/hcs/{site}/r4/MedicationDispense/{id}")
  public MedicationDispense medicationDispenseRead(
      HttpServletRequest request,
      @PathVariable("site") String site,
      @PathVariable("id") String id) {
    MedicationDispenseId identifier = parseOrDie(id);
    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(PatientId.forIcn(identifier.vistaId().patientIdentifier()))
            .type(Set.of(Domains.meds))
            .id(Optional.of(identifier.vistaId().recordId()))
            .build();
    var charonResponse = charonClient.request(vprGetPatientData(site, rpcRequest));
    var resources =
        transformation(
                site,
                identifier.vistaId().patientIdentifier(),
                acceptOnlyWithFillDateEqualTo(identifier.fillDate()))
            .toResource()
            .apply(charonResponse.value());
    return verifyAndGetResult(resources, id);
  }

  /** Search for medication dispense records by patient. */
  @SneakyThrows
  @GetMapping(value = "/hcs/{site}/r4/MedicationDispense")
  public MedicationDispense.Bundle medicationDispenseSearch(
      HttpServletRequest request,
      @PathVariable(value = "site") String site,
      @RequestParam(name = "patient") String patientIcn,
      @RequestParam(name = "whenprepared", required = false) @Size(max = 2) String[] whenPrepared,
      @RequestParam(
              value = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count) {
    var fillFilter =
        DateSearchBoundaries.optionallyOf(whenPrepared)
            .map(R4MedicationDispenseTransformer::acceptOnlyWithFillDateInRange)
            .orElse(acceptAll());
    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(PatientId.forIcn(patientIcn))
            .type(Set.of(Domains.meds))
            .build();
    var charonResponse = charonClient.request(vprGetPatientData(site, rpcRequest));
    return bundlerFactory
        .forTransformation(transformation(site, patientIcn, fillFilter))
        .site(charonResponse.invocationResult().vista())
        .bundling(
            R4Bundling.newBundle(MedicationDispense.Bundle::new)
                .newEntry(MedicationDispense.Entry::new)
                .build())
        .resourceType(MedicationDispense.class.getSimpleName())
        .request(request)
        .build()
        .apply(charonResponse.value());
  }

  private MedicationDispenseId parseOrDie(String publicId) {
    String identifier;
    MedicationDispenseId medicationDispenseId;
    try {
      identifier = witnessProtection.toPrivateId(publicId);
      medicationDispenseId = MedicationDispenseId.fromString(identifier);
    } catch (IdEncoder.BadId | MedicationDispenseId.MalformedId | IllegalArgumentException e) {
      throw ResourceExceptions.NotFound.because("Could not unpack id: " + publicId);
    }
    if (medicationDispenseId.vistaId().vprRpcDomain().equals(Domains.meds)
        && medicationDispenseId.fillDate().isEmpty()) {
      throw new ResourceExceptions.NotFound(publicId + " is an invalid id for the meds domain");
    }

    return medicationDispenseId;
  }

  private R4Transformation<Results, MedicationDispense> transformation(
      String site, String patientId, Predicate<Fill> fillFilter) {
    return R4Transformation.<VprGetPatientData.Response.Results, MedicationDispense>builder()
        .toResource(
            rpcResults ->
                R4MedicationDispenseTransformer.builder()
                    .site(site)
                    .patientIcn(patientId)
                    .rpcResults(rpcResults)
                    .fillFilter(fillFilter)
                    .build()
                    .toFhir()
                    .collect(toList()))
        .build();
  }
}
