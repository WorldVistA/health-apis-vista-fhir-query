package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.dieOnError;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Bundle;
import gov.va.api.health.r4.api.resources.Coverage.Entry;
import gov.va.api.health.vistafhirquery.service.api.R4CoverageApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonResponse;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch.Request;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import java.time.ZoneId;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Builder
@Validated
@RestController
@RequestMapping(produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
public class R4SiteCoverageController implements R4CoverageApi {
  private final R4BundlerFactory bundlerFactory;

  private final CharonClient charon;

  private final WitnessProtection witnessProtection;

  public static Request coverageByPatientIcn(String patientIcn) {
    return Request.builder().id(Request.PatientId.forIcn(patientIcn)).build();
  }

  @Override
  @PostMapping(value = "/site/{site}/r4/Coverage")
  public void coverageCreate(
      HttpServletResponse response,
      @PathVariable(value = "site") String site,
      @RequestBody Coverage body) {
    var newResourceUrl = "/site/" + site + "/r4/Coverage/{new-resource-id}";
    response.addHeader("Location", newResourceUrl);
  }

  @Override
  @GetMapping(value = "/site/{site}/r4/Coverage/{publicId}")
  public Coverage coverageRead(
      @PathVariable(value = "site") String site, @PathVariable(value = "publicId") String id) {
    var coordinates =
        PatientTypeCoordinates.fromString(
            witnessProtection.privateIdForResourceOrDie(id, Coverage.class));
    var request = lighthouseRpcGatewayRequest(site, manifestRequest(coordinates));
    var response = charon.request(request);
    var lhsResponse = lighthouseRpcGatewayResponse(response);
    dieOnError(lhsResponse);
    var resources =
        transformation(response.timezoneAsZoneId(), coordinates.icn())
            .toResource()
            .apply(lhsResponse);
    return verifyAndGetResult(resources, id);
  }

  /** Search support. */
  @Override
  @GetMapping("/site/{site}/r4/Coverage")
  public Coverage.Bundle coverageSearch(
      HttpServletRequest httpRequest,
      @PathVariable(value = "site") String site,
      @RequestParam(value = "patient") String patientIcn,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "_count", required = false) Integer count) {
    var request = lighthouseRpcGatewayRequest(site, coverageByPatientIcn(patientIcn));
    var response = charon.request(request);
    return toBundle(httpRequest, response).apply(lighthouseRpcGatewayResponse(response));
  }

  private LhsLighthouseRpcGatewayGetsManifest.Request manifestRequest(
      PatientTypeCoordinates coordinates) {
    return LhsLighthouseRpcGatewayGetsManifest.Request.builder()
        .file(InsuranceType.FILE_NUMBER)
        .iens(coordinates.recordId())
        .fields(
            List.of(
                InsuranceType.INSURANCE_TYPE,
                InsuranceType.GROUP_PLAN,
                InsuranceType.COORDINATION_OF_BENEFITS,
                InsuranceType.INSURANCE_EXPIRATION_DATE,
                InsuranceType.STOP_POLICY_FROM_BILLING,
                InsuranceType.PT_RELATIONSHIP_HIPAA,
                InsuranceType.PHARMACY_PERSON_CODE,
                InsuranceType.SUBSCRIBER_ID,
                InsuranceType.EFFECTIVE_DATE_OF_POLICY))
        .flags(
            List.of(
                GetsManifestFlags.OMIT_NULL_VALUES,
                GetsManifestFlags.RETURN_INTERNAL_VALUES,
                GetsManifestFlags.RETURN_EXTERNAL_VALUES))
        .build();
  }

  private R4Bundler<LhsLighthouseRpcGatewayResponse, Coverage, Entry, Bundle> toBundle(
      HttpServletRequest request, CharonResponse<Request, Results> response) {
    return bundlerFactory
        .forTransformation(
            transformation(response.timezoneAsZoneId(), request.getParameter("patient")))
        .site(response.invocationResult().vista())
        .bundling(R4Bundling.newBundle(Coverage.Bundle::new).newEntry(Coverage.Entry::new).build())
        .resourceType("Coverage")
        .request(request)
        .build();
  }

  private R4Transformation<LhsLighthouseRpcGatewayResponse, Coverage> transformation(
      ZoneId zoneId, String patientId) {
    return R4Transformation.<LhsLighthouseRpcGatewayResponse, Coverage>builder()
        .toResource(
            rpcResponse ->
                rpcResponse.resultsByStation().entrySet().parallelStream()
                    .flatMap(
                        rpcResults ->
                            R4CoverageTransformer.builder()
                                .patientIcn(patientId)
                                .vistaZoneId(zoneId)
                                .rpcResults(rpcResults)
                                .build()
                                .toFhir())
                    .collect(toList()))
        .build();
  }
}