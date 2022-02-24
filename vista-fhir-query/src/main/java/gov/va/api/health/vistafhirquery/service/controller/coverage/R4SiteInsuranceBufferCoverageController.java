package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;
import static gov.va.api.health.vistafhirquery.service.charonclient.LhsGatewayErrorHandler.dieOnReadError;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.updateResponseForCreatedPatientCentricResource;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.referenceIdFromUri;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.recordcontext.CreatePatientRecordWriteContext;
import gov.va.api.health.vistafhirquery.service.controller.recordcontext.PatientRecordWriteContext;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PatientId;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Builder
@Validated
@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@RequestMapping(produces = {"application/json", "application/fhir+json"})
public class R4SiteInsuranceBufferCoverageController {
  private final R4BundlerFactory bundlerFactory;

  private final CharonClient charon;

  private final WitnessProtection witnessProtection;

  private static String beneficiaryOrDie(Coverage body) {
    return referenceIdFromUri(body.beneficiary())
        .orElseThrow(
            () ->
                RequestPayloadExceptions.MissingRequiredField.builder()
                    .jsonPath(".beneficiary.reference")
                    .build());
  }

  /** Create Support. */
  @PostMapping(
      value = "/hcs/{site}/r4/Coverage",
      consumes = {"application/json", "application/fhir+json"})
  public void coverageCreate(
      @Redact HttpServletResponse response, String site, @Redact Coverage body) {
    var ctx =
        updateOrCreate(
            CreatePatientRecordWriteContext.<Coverage>builder()
                .fileNumber(InsuranceVerificationProcessor.FILE_NUMBER)
                .site(site)
                .body(body)
                .patientIcn(beneficiaryOrDie(body))
                .build());
    var resourceId = witnessProtection.toPublicId(Coverage.class, ctx.newResourceId());
    var newResourceUrl =
        bundlerFactory
            .linkProperties()
            .r4()
            .readUrl(site, Coverage.class.getSimpleName(), resourceId);
    updateResponseForCreatedPatientCentricResource(response, ctx.patientIcn(), newResourceUrl);
  }

  /** Read support. */
  @GetMapping(value = "/hcs/{site}/r4/Coverage/{publicId}")
  public Coverage coverageRead(
      @PathVariable(value = "site") String site, @PathVariable(value = "publicId") String id) {
    var coordinates =
        witnessProtection.toPatientTypeCoordinatesOrDie(
            id, Coverage.class, InsuranceVerificationProcessor.FILE_NUMBER);
    if (!InsuranceVerificationProcessor.FILE_NUMBER.equals(coordinates.file())) {
      throw new ResourceExceptions.NotFound(
          "Expected the ids file number to match the "
              + "InsuranceVerificationProcessor file, but it did not: "
              + id);
    }
    var request = lighthouseRpcGatewayRequest(site, manifestRequest(coordinates));
    var response = charon.request(request);
    var lhsResponse = lighthouseRpcGatewayResponse(response);
    dieOnReadError(lhsResponse);
    var resources = transformation(site, coordinates.icn()).toResource().apply(lhsResponse);
    return verifyAndGetResult(resources, id);
  }

  private LhsLighthouseRpcGatewayCoverageWrite.Request coverageWriteDetails(
      LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi operation,
      String patient,
      Coverage body) {
    var fieldsToWrite =
        R4CoverageToInsuranceBufferTransformer.builder().coverage(body).build().toInsuranceBuffer();
    return LhsLighthouseRpcGatewayCoverageWrite.Request.builder()
        .api(operation)
        .patient(PatientId.forIcn(patient))
        .fields(fieldsToWrite)
        .build();
  }

  private LhsLighthouseRpcGatewayGetsManifest.Request manifestRequest(
      PatientTypeCoordinates coordinates) {
    return LhsLighthouseRpcGatewayGetsManifest.Request.builder()
        .file(InsuranceVerificationProcessor.FILE_NUMBER)
        .iens(coordinates.ien())
        .fields(InsuranceBufferToR4CoverageTransformer.MAPPED_VISTA_FIELDS)
        .flags(
            List.of(
                LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags.OMIT_NULL_VALUES,
                LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags
                    .RETURN_INTERNAL_VALUES,
                LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags
                    .RETURN_EXTERNAL_VALUES))
        .build();
  }

  private R4Transformation<LhsLighthouseRpcGatewayResponse, Coverage> transformation(
      String site, String patientIcn) {
    return R4Transformation.<LhsLighthouseRpcGatewayResponse, Coverage>builder()
        .toResource(
            rpcResponse ->
                rpcResponse.resultsByStation().entrySet().parallelStream()
                    .flatMap(
                        rpcResults ->
                            InsuranceBufferToR4CoverageTransformer.builder()
                                .patientIcn(patientIcn)
                                .site(site)
                                .results(rpcResults.getValue())
                                .build()
                                .toFhir())
                    .collect(toList()))
        .build();
  }

  private <C extends PatientRecordWriteContext<Coverage>> C updateOrCreate(C ctx) {
    var charonRequest =
        lighthouseRpcGatewayRequest(
            ctx.site(), coverageWriteDetails(ctx.coverageWriteApi(), ctx.patientIcn(), ctx.body()));
    var charonResponse = charon.request(charonRequest);
    var lhsResponse = lighthouseRpcGatewayResponse(charonResponse);
    ctx.result(lhsResponse);
    return ctx;
  }
}
