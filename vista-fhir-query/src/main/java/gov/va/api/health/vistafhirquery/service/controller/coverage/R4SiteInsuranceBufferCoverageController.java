package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.updateResponseForCreatedResource;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.referenceIdFromUri;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.recordcontext.CreatePatientRecordWriteContext;
import gov.va.api.health.vistafhirquery.service.controller.recordcontext.PatientRecordWriteContext;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PatientId;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
    updateResponseForCreatedResource(response, newResourceUrl);
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
