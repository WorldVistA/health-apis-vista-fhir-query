package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.dieOnError;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.api.R4InsurancePlanApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.writes.CreateNonPatientRecordWriteContext;
import gov.va.api.health.vistafhirquery.service.controller.writes.UpdateNonPatientRecordWriteContext;
import gov.va.api.health.vistafhirquery.service.controller.writes.WriteContext;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.GroupInsurancePlan;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Builder
@Validated
@RestController
@RequestMapping(produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@Slf4j
public class R4SiteInsurancePlanController implements R4InsurancePlanApi {
  private final R4BundlerFactory bundlerFactory;

  private final WitnessProtection witnessProtection;

  private final CharonClient charon;

  /** Create A request based off of record coordinates. */
  public static LhsLighthouseRpcGatewayGetsManifest.Request manifestRequest(
      RecordCoordinates coordinates) {
    return LhsLighthouseRpcGatewayGetsManifest.Request.builder()
        .file(GroupInsurancePlan.FILE_NUMBER)
        .iens(coordinates.ien())
        .fields(R4InsurancePlanTransformer.REQUIRED_FIELDS)
        .flags(
            List.of(
                GetsManifestFlags.OMIT_NULL_VALUES,
                GetsManifestFlags.RETURN_INTERNAL_VALUES,
                GetsManifestFlags.RETURN_EXTERNAL_VALUES))
        .build();
  }

  private void insuranceFileOrDie(String id, RecordCoordinates recordCoordinates) {
    if (!GroupInsurancePlan.FILE_NUMBER.equals(recordCoordinates.file())) {
      throw new NotFound(id);
    }
  }

  @Override
  @PostMapping(
      value = "/site/{site}/r4/InsurancePlan",
      consumes = {"application/json", "application/fhir+json"})
  public void insurancePlanCreate(
      @Redact HttpServletResponse response,
      @PathVariable(value = "site") String site,
      @Redact @RequestBody InsurancePlan body) {
    var ctx =
        updateOrCreate(
            CreateNonPatientRecordWriteContext.<InsurancePlan>builder()
                .fileNumber(GroupInsurancePlan.FILE_NUMBER)
                .site(site)
                .body(body)
                .build());
    var newResourceUrl =
        bundlerFactory
            .linkProperties()
            .r4()
            .readUrl(
                site,
                "InsurancePlan",
                witnessProtection.toPublicId(InsurancePlan.class, ctx.newResourceId()));
    response.addHeader(HttpHeaders.LOCATION, newResourceUrl);
    response.setStatus(201);
  }

  @Override
  @GetMapping(value = "/site/{site}/r4/InsurancePlan/{publicId}")
  @SneakyThrows
  public InsurancePlan insurancePlanRead(
      @PathVariable(value = "site") String site, @PathVariable(value = "publicId") String id) {
    var coordinates = witnessProtection.toRecordCoordinates(id);
    insuranceFileOrDie(id, coordinates);
    log.info(
        "Looking for record {} in file {} at site {}",
        coordinates.ien(),
        coordinates.file(),
        coordinates.site());
    var request = lighthouseRpcGatewayRequest(site, manifestRequest(coordinates));
    var response = charon.request(request);
    var lhsResponse = lighthouseRpcGatewayResponse(response);
    dieOnError(lhsResponse);
    var resources = transformation().toResource().apply(lhsResponse);
    return verifyAndGetResult(resources, id);
  }

  @Override
  @PutMapping(
      value = "/site/{site}/r4/InsurancePlan/{id}",
      consumes = {"application/json", "application/fhir+json"})
  public void insurancePlanUpdate(
      @Redact HttpServletResponse response,
      @PathVariable(value = "site") String site,
      @PathVariable(value = "id") String id,
      @Redact @RequestBody InsurancePlan body) {
    var existingRecordCoordinates = witnessProtection.toRecordCoordinates(id);
    insuranceFileOrDie(id, existingRecordCoordinates);
    if (isBlank(body.id()) || !existingRecordCoordinates.toString().equals(body.id())) {
      throw ResourceExceptions.CannotUpdateResourceWithMismatchedIds.because(
          existingRecordCoordinates.toString(), body.id());
    }
    updateOrCreate(
        UpdateNonPatientRecordWriteContext.<InsurancePlan>builder()
            .fileNumber(GroupInsurancePlan.FILE_NUMBER)
            .site(site)
            .body(body)
            .existingRecord(existingRecordCoordinates)
            .build());
    response.setStatus(200);
  }

  private LhsLighthouseRpcGatewayCoverageWrite.Request insurancePlanWriteDetails(
      CoverageWriteApi operation, InsurancePlan body) {
    var fieldsToWrite =
        R4InsurancePlanToGroupInsurancePlanFileTransformer.builder()
            .insurancePlan(body)
            .build()
            .toGroupInsurancePlanFile();
    return LhsLighthouseRpcGatewayCoverageWrite.Request.builder()
        .api(operation)
        .fields(fieldsToWrite)
        .build();
  }

  private R4Transformation<LhsLighthouseRpcGatewayResponse, InsurancePlan> transformation() {
    return R4Transformation.<LhsLighthouseRpcGatewayResponse, InsurancePlan>builder()
        .toResource(
            rpcResponse ->
                rpcResponse.resultsByStation().entrySet().parallelStream()
                    .flatMap(
                        rpcResults ->
                            R4InsurancePlanTransformer.builder()
                                .rpcResults(rpcResults)
                                .build()
                                .toFhir())
                    .collect(toList()))
        .build();
  }

  private <C extends WriteContext<InsurancePlan>> C updateOrCreate(C ctx) {
    var charonRequest =
        lighthouseRpcGatewayRequest(
            ctx.site(), insurancePlanWriteDetails(ctx.coverageWriteApi(), ctx.body()));
    var charonResponse = charon.request(charonRequest);
    var lhsResponse = lighthouseRpcGatewayResponse(charonResponse);
    ctx.result(lhsResponse);
    return ctx;
  }
}
