package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.dieOnError;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifySiteSpecificVistaResponseOrDie;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.api.R4InsurancePlanApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.charonclient.LhsGatewayErrorHandler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
            InsurancePlanWriteContext.builder()
                .site(site)
                .body(body)
                .mode(CoverageWriteApi.CREATE)
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
    var coordinates = witnessProtection.toRecordCoordinates(id);
    insuranceFileOrDie(id, coordinates);
    if (isBlank(body.id()) || !coordinates.toString().equals(body.id())) {
      throw ResourceExceptions.CannotUpdateResourceWithMismatchedIds.because(
          coordinates.toString(), body.id());
    }
    updateOrCreate(
        InsurancePlanWriteContext.builder()
            .site(site)
            .body(body)
            .mode(CoverageWriteApi.UPDATE)
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

  private InsurancePlanWriteContext updateOrCreate(InsurancePlanWriteContext ctx) {
    var charonRequest =
        lighthouseRpcGatewayRequest(ctx.site(), insurancePlanWriteDetails(ctx.mode(), ctx.body()));
    var charonResponse = charon.request(charonRequest);
    var lhsResponse = lighthouseRpcGatewayResponse(charonResponse);
    return ctx.result(lhsResponse);
  }

  @Getter
  @Setter
  private static class InsurancePlanWriteContext {
    private final String site;

    private final InsurancePlan body;

    private final String fileNumber;

    private final CoverageWriteApi mode;

    private LhsLighthouseRpcGatewayResponse.FilemanEntry result;

    @Builder
    public InsurancePlanWriteContext(String site, InsurancePlan body, CoverageWriteApi mode) {
      this.site = site;
      this.body = body;
      this.mode = mode;
      this.fileNumber = GroupInsurancePlan.FILE_NUMBER;
    }

    String newResourceId() {
      return RecordCoordinates.builder()
          .site(site())
          .ien(result().ien())
          .file(fileNumber())
          .build()
          .toString();
    }

    InsurancePlanWriteContext result(LhsLighthouseRpcGatewayResponse response) {
      verifySiteSpecificVistaResponseOrDie(site(), response);
      var resultsForStation = response.resultsByStation().get(site());
      LhsGatewayErrorHandler.of(resultsForStation).validateResults();
      var results = resultsForStation.results();
      var insTypeResults =
          resultsForStation.results().stream()
              .filter(entry -> fileNumber.equals(entry.file()))
              .toList();
      if (insTypeResults.size() != 1) {
        throw ExpectationFailed.because("Unexpected number of results: " + results.size());
      }
      if ("1".equals(insTypeResults.get(0).status())) {
        this.result = insTypeResults.get(0);
        return this;
      }
      throw ExpectationFailed.because(
          "Unexpected status code from results: " + insTypeResults.get(0).status());
    }
  }
}
