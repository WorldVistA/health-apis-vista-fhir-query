package gov.va.api.health.vistafhirquery.service.controller.appointment;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.vprGetPatientData;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.toLocalDateMacroString;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.ids.client.IdEncoder;
import gov.va.api.health.r4.api.resources.Appointment;
import gov.va.api.health.vistafhirquery.service.api.R4AppointmentApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.health.vistafhirquery.service.controller.DateSearchBoundaries;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData;
import gov.va.api.lighthouse.charon.models.vprgetpatientdata.VprGetPatientData.Request.PatientId;
import java.util.Optional;
import java.util.Set;
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
 * Request Mappings for Appointment Profile using a VistA backend.
 *
 * @implSpec https://hl7.org/fhir/R4/appointment.html
 */
@Validated
@RestController
@RequestMapping(produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@Builder
public class R4SiteAppointmentController implements R4AppointmentApi {
  private final R4BundlerFactory bundlerFactory;

  private final CharonClient charonClient;

  private final VistaApiConfig vistaApiConfig;

  private final WitnessProtection witnessProtection;

  /** Read by id. */
  @SneakyThrows
  @GetMapping(value = "/hcs/{site}/r4/Appointment/{id}")
  public Appointment appointmentRead(
      HttpServletRequest request,
      @PathVariable("site") String site,
      @PathVariable("id") String id) {
    SegmentedVistaIdentifier ids = parseOrDie(id);
    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(PatientId.forIcn(ids.patientIdentifier()))
            .type(Set.of(VprGetPatientData.Domains.appointments))
            .id(Optional.of(ids.recordId()))
            .build();
    var charonResponse = charonClient.request(vprGetPatientData(site, rpcRequest));
    var resources =
        transformation(site, ids.patientIdentifier()).toResource().apply(charonResponse.value());
    return verifyAndGetResult(resources, id);
  }

  /** Search for appointment records by patient. */
  @SneakyThrows
  @GetMapping(value = "/hcs/{site}/r4/Appointment")
  public Appointment.Bundle appointmentSearch(
      HttpServletRequest request,
      @PathVariable(value = "site") String site,
      @RequestParam(name = "date", required = false) @Size(max = 2) String[] date,
      @RequestParam(name = "patient") String patientIcn,
      @RequestParam(
              value = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count) {
    if (date == null || date.length == 0) {
      date = new String[] {"ge1900"};
    }
    DateSearchBoundaries boundaries = DateSearchBoundaries.of(date);
    var rpcRequest =
        VprGetPatientData.Request.builder()
            .context(Optional.ofNullable(vistaApiConfig.getVprGetPatientDataContext()))
            .dfn(PatientId.forIcn(patientIcn))
            .type(Set.of(VprGetPatientData.Domains.appointments))
            .start(toLocalDateMacroString(boundaries.start()))
            .stop(toLocalDateMacroString(boundaries.stop()))
            .build();
    var charonResponse = charonClient.request(vprGetPatientData(site, rpcRequest));
    return bundlerFactory
        .forTransformation(transformation(site, patientIcn))
        .site(charonResponse.invocationResult().vista())
        .bundling(
            R4Bundling.newBundle(Appointment.Bundle::new).newEntry(Appointment.Entry::new).build())
        .resourceType("Appointment")
        .request(request)
        .build()
        .apply(charonResponse.value());
  }

  private SegmentedVistaIdentifier parseOrDie(String publicId) {
    try {
      return SegmentedVistaIdentifier.unpack(witnessProtection.toPrivateId(publicId));
    } catch (IdEncoder.BadId | IllegalArgumentException e) {
      throw ResourceExceptions.NotFound.because("Could not unpack id: " + publicId);
    }
  }

  private R4Transformation<VprGetPatientData.Response.Results, Appointment> transformation(
      String site, String patientId) {
    return R4Transformation.<VprGetPatientData.Response.Results, Appointment>builder()
        .toResource(
            rpcResults ->
                R4AppointmentTransformer.builder()
                    .site(site)
                    .patientIcn(patientId)
                    .rpcResults(rpcResults)
                    .build()
                    .toFhir()
                    .collect(toList()))
        .build();
  }
}
