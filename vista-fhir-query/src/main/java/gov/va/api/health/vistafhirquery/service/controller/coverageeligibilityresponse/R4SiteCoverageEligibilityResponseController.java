package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;
import static gov.va.api.health.vistafhirquery.service.controller.coverage.R4SiteCoverageController.coverageByPatientIcn;
import static gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PatientId.forIcn;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse;
import gov.va.api.health.vistafhirquery.service.api.R4CoverageEligibilityResponseApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.controller.PatientTypeCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageEligibilityResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayListManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.PlanCoverageLimitations;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@RequestMapping(produces = {"application/json", "application/fhir+json"})
public class R4SiteCoverageEligibilityResponseController
    implements R4CoverageEligibilityResponseApi {
  private final R4BundlerFactory bundlerFactory;

  private final CharonClient charon;

  /** Create A request based off of record coordinates. */
  public static LhsLighthouseRpcGatewayCoverageEligibilityResponse.Request manifestRequest(
      PatientTypeCoordinates coordinates) {
    return LhsLighthouseRpcGatewayCoverageEligibilityResponse.Request.read()
        .iens(coordinates.ien())
        .patientId(forIcn(coordinates.icn()))
        .build();
  }

  private void addCoverageResultsToContext(R4CoverageEligibilityResponseSearchContext ctx) {
    var charonRequest =
        lighthouseRpcGatewayRequest(ctx.site(), coverageByPatientIcn(ctx.patientIcn()));
    var charonResponse = charon.request(charonRequest);
    ctx.coverageResults(
        lighthouseRpcGatewayResponse(charonResponse).resultsByStation().get(ctx.site()));
  }

  private void addPlanLimitationsToContext(R4CoverageEligibilityResponseSearchContext ctx) {
    List<String> fields = new ArrayList<>();
    fields.addAll(R4CoverageEligibilityResponseTransformer.REQUIRED_FIELDS);
    var details =
        LhsLighthouseRpcGatewayListManifest.Request.builder()
            .file(PlanCoverageLimitations.FILE_NUMBER)
            .fields(fields)
            .build();
    var charonRequest = lighthouseRpcGatewayRequest(ctx.site(), details);
    var charonResponse = charon.request(charonRequest);
    ctx.planLimitationsResults(
        lighthouseRpcGatewayResponse(charonResponse).resultsByStation().get(ctx.site()));
  }

  @Override
  @PostMapping(
      value = "/hcs/{site}/r4/CoverageEligibilityResponse",
      consumes = {"application/json", "application/fhir+json"})
  public void coverageEligibilityResponseCreate(
      @Redact HttpServletResponse response,
      @PathVariable(value = "site") String site,
      @Redact @RequestBody CoverageEligibilityResponse body) {
    var newResourceUrl =
        bundlerFactory
            .linkProperties()
            .r4()
            .readUrl(site, "CoverageEligibilityResponse", "<new-resource-id>");
    response.addHeader(HttpHeaders.LOCATION, newResourceUrl);
    response.setStatus(201);
  }

  /** Search support. */
  @Override
  @GetMapping(value = "/hcs/{site}/r4/CoverageEligibilityResponse")
  public CoverageEligibilityResponse.Bundle coverageEligibilityResponseSearch(
      @Redact HttpServletRequest httpRequest,
      @NonNull @PathVariable(value = "site") String site,
      @RequestParam(value = "patient") String icn,
      @RequestParam(
              value = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count) {
    var searchCtx =
        R4CoverageEligibilityResponseSearchContext.builder().site(site).patientIcn(icn).build();
    addCoverageResultsToContext(searchCtx);
    addPlanLimitationsToContext(searchCtx);
    return toBundle(httpRequest, site).apply(searchCtx);
  }

  private R4Bundler<
          R4CoverageEligibilityResponseSearchContext,
          CoverageEligibilityResponse,
          CoverageEligibilityResponse.Entry,
          CoverageEligibilityResponse.Bundle>
      toBundle(HttpServletRequest request, String site) {
    return bundlerFactory
        .forTransformation(transformation())
        .site(site)
        .bundling(
            R4Bundling.newBundle(CoverageEligibilityResponse.Bundle::new)
                .newEntry(CoverageEligibilityResponse.Entry::new)
                .build())
        .resourceType("CoverageEligibilityResponse")
        .request(request)
        .build();
  }

  private R4Transformation<R4CoverageEligibilityResponseSearchContext, CoverageEligibilityResponse>
      transformation() {
    return R4Transformation
        .<R4CoverageEligibilityResponseSearchContext, CoverageEligibilityResponse>builder()
        .toResource(
            ctx ->
                R4CoverageEligibilityResponseTransformer.builder()
                    .searchContext(ctx)
                    .build()
                    .toFhir()
                    .toList())
        .build();
  }
}
