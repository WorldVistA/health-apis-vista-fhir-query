package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.dieOnError;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.ignoreIdForCreate;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.updateResponseForCreatedResource;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.referenceIdFromUri;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.autoconfig.logging.Redact;
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
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.CannotUpdateResourceWithMismatchedIds;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.writes.CreatePatientRecordWriteContext;
import gov.va.api.health.vistafhirquery.service.controller.writes.PatientRecordWriteContext;
import gov.va.api.health.vistafhirquery.service.controller.writes.UpdatePatientRecordWriteContext;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageSearch.Request;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PatientId;
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
import org.springframework.web.bind.annotation.PutMapping;
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

  private static String beneficiaryOrDie(Coverage body) {
    return referenceIdFromUri(body.beneficiary())
        .orElseThrow(() -> BadRequestPayload.because("Beneficiary reference not found."));
  }

  public static Request coverageByPatientIcn(String patientIcn) {
    return Request.builder().id(PatientId.forIcn(patientIcn)).build();
  }

  @Override
  @PostMapping(
      value = "/site/{site}/r4/Coverage",
      consumes = {"application/json", "application/fhir+json"})
  public void coverageCreate(
      @Redact HttpServletResponse response,
      @PathVariable(value = "site") String site,
      @Redact @RequestBody Coverage body) {
    ignoreIdForCreate(body);
    var ctx =
        updateOrCreate(
            CreatePatientRecordWriteContext.<Coverage>builder()
                .fileNumber(InsuranceType.FILE_NUMBER)
                .site(site)
                .body(body)
                .patientIcn(beneficiaryOrDie(body))
                .build());
    var newResourceUrl =
        bundlerFactory
            .linkProperties()
            .r4()
            .readUrl(
                site,
                "Coverage",
                witnessProtection.toPublicId(Coverage.class, ctx.newResourceId()));
    updateResponseForCreatedResource(response, newResourceUrl);
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
      @RequestParam(
              value = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count) {
    var request = lighthouseRpcGatewayRequest(site, coverageByPatientIcn(patientIcn));
    var response = charon.request(request);
    return toBundle(httpRequest, response).apply(lighthouseRpcGatewayResponse(response));
  }

  @Override
  @PutMapping(
      value = "/site/{site}/r4/Coverage/{id}",
      consumes = {"application/json", "application/fhir+json"})
  public void coverageUpdate(
      @Redact HttpServletResponse response,
      @PathVariable(value = "site") String site,
      @PathVariable(value = "id") String id,
      @Redact @RequestBody Coverage body) {
    var privateId = witnessProtection.privateIdForResourceOrDie(id, Coverage.class);
    PatientTypeCoordinates existingRecordCoordinates = PatientTypeCoordinates.fromString(privateId);
    var ctx =
        UpdatePatientRecordWriteContext.<Coverage>builder()
            .fileNumber(InsuranceType.FILE_NUMBER)
            .site(site)
            .body(body)
            .patientIcn(beneficiaryOrDie(body))
            .existingRecord(existingRecordCoordinates)
            .build();
    updateValidationRules().forEach(rule -> rule.test(ctx));
    updateOrCreate(ctx);
    response.setStatus(200);
  }

  private LhsLighthouseRpcGatewayCoverageWrite.Request coverageWriteDetails(
      CoverageWriteApi operation, String patient, Coverage body) {
    var fieldsToWrite =
        R4CoverageToInsuranceTypeFileTransformer.builder()
            .coverage(body)
            .build()
            .toInsuranceTypeFile();
    return LhsLighthouseRpcGatewayCoverageWrite.Request.builder()
        .api(operation)
        .patient(PatientId.forIcn(patient))
        .fields(fieldsToWrite)
        .build();
  }

  private LhsLighthouseRpcGatewayGetsManifest.Request manifestRequest(
      PatientTypeCoordinates coordinates) {
    return LhsLighthouseRpcGatewayGetsManifest.Request.builder()
        .file(InsuranceType.FILE_NUMBER)
        .iens(coordinates.recordId())
        .fields(R4CoverageTransformer.REQUIRED_FIELDS)
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

  private <C extends PatientRecordWriteContext<Coverage>> C updateOrCreate(C ctx) {
    /*
     * ToDo File 2.312 SOURCE OF INFORMATION field #1.09 is 22 for the WellHive interface.
     *   https://vajira.max.gov/browse/API-10035
     */
    var charonRequest =
        lighthouseRpcGatewayRequest(
            ctx.site(), coverageWriteDetails(ctx.coverageWriteApi(), ctx.patientIcn(), ctx.body()));
    var charonResponse = charon.request(charonRequest);
    var lhsResponse = lighthouseRpcGatewayResponse(charonResponse);
    ctx.result(lhsResponse);
    return ctx;
  }

  private List<ContextValidationRule> updateValidationRules() {
    return List.of(this::validateIdsMatch, this::validatePatientsMatch, this::validateSitesMatch);
  }

  private void validateIdsMatch(UpdatePatientRecordWriteContext<Coverage> ctx) {
    var idFromBody = ctx.body().id();
    var idFromUrl = ctx.existingRecord().toString();
    if (isBlank(idFromBody) || !idFromUrl.equals(idFromBody)) {
      throw CannotUpdateResourceWithMismatchedIds.because(idFromUrl, idFromBody);
    }
  }

  private void validatePatientsMatch(UpdatePatientRecordWriteContext<Coverage> ctx) {
    var icnFromBody = ctx.patientIcn();
    var icnFromId = ctx.existingRecord().icn();
    if (!icnFromBody.equals(icnFromId)) {
      throw ExpectationFailed.because(
          "Patient ICNs do not match: IcnFromBody(%s), IcnFromIdCoordinates(%s)",
          icnFromBody, icnFromId);
    }
  }

  private void validateSitesMatch(UpdatePatientRecordWriteContext<Coverage> ctx) {
    var siteFromUrl = ctx.site();
    var siteFromId = ctx.existingRecord().siteId();
    if (!siteFromId.equals(siteFromUrl)) {
      throw ExpectationFailed.because(
          "Site ids do not match: SiteFromIdCoordinates(%s), SiteFromUrl(%s)",
          siteFromId, siteFromUrl);
    }
  }

  @FunctionalInterface
  interface ContextValidationRule {
    void test(UpdatePatientRecordWriteContext<Coverage> ctx);
  }
}
