package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;
import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayResponse;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.dieOnError;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.ignoreIdForCreate;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.updateResponseForCreatedResource;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifySiteSpecificVistaResponseOrDie;
import static gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.api.R4OrganizationApi;
import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.charonclient.LhsGatewayErrorHandler;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.RecordCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.Request.CoverageWriteApi;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayListManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.Payer;
import java.util.List;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class R4SiteOrganizationController implements R4OrganizationApi {
  private final R4BundlerFactory bundlerFactory;

  private final WitnessProtection witnessProtection;

  private final CharonClient charon;

  /** Create A request based off of record coordinates. */
  public static LhsLighthouseRpcGatewayGetsManifest.Request manifestRequest(
      RecordCoordinates coordinates) {
    return LhsLighthouseRpcGatewayGetsManifest.Request.builder()
        .file(coordinates.file())
        .iens(coordinates.ien())
        .fields(requiredFieldsForFile(coordinates.file()))
        .flags(
            List.of(
                GetsManifestFlags.OMIT_NULL_VALUES,
                GetsManifestFlags.RETURN_INTERNAL_VALUES,
                GetsManifestFlags.RETURN_EXTERNAL_VALUES))
        .build();
  }

  private static List<String> requiredFieldsForFile(String fileNumber) {
    switch (fileNumber) {
      case InsuranceCompany.FILE_NUMBER:
        return R4OrganizationTransformer.REQUIRED_FIELDS;
      case Payer.FILE_NUMBER:
        return R4OrganizationPayerTransformer.REQUIRED_FIELDS;
      default:
        throw new IllegalArgumentException(
            "Unsupported file for Organization request: " + fileNumber);
    }
  }

  private LhsLighthouseRpcGatewayListManifest.Request createRpcGatewayRequestForType(String type) {
    if (isBlank(type)) {
      return null;
    }
    switch (type) {
      case "ins":
        return LhsLighthouseRpcGatewayListManifest.Request.builder()
            .file(InsuranceCompany.FILE_NUMBER)
            .fields(R4OrganizationTransformer.REQUIRED_FIELDS)
            .build();
      case "pay":
        return LhsLighthouseRpcGatewayListManifest.Request.builder()
            .file(Payer.FILE_NUMBER)
            .fields(R4OrganizationPayerTransformer.REQUIRED_FIELDS)
            .build();
      default:
        return null;
    }
  }

  @Override
  @PostMapping(
      value = "/site/{site}/r4/Organization",
      consumes = {"application/json", "application/fhir+json"})
  public void organizationCreate(
      @Redact HttpServletResponse response,
      @PathVariable(value = "site") String site,
      @Redact @RequestBody Organization body) {
    ignoreIdForCreate(body);
    var ctx =
        updateOrCreate(
            OrganizationWriteContext.builder()
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
                "Organization",
                witnessProtection.toPublicId(Organization.class, ctx.newResourceId()));
    updateResponseForCreatedResource(response, newResourceUrl);
  }

  @Override
  @GetMapping(value = "/site/{site}/r4/Organization/{publicId}")
  public Organization organizationRead(
      @PathVariable(value = "site") String site, @PathVariable(value = "publicId") String id) {
    var coordinates = witnessProtection.toRecordCoordinates(id);
    supportedVistaFileOrDie(id, coordinates);
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
  @GetMapping(value = "/site/{site}/r4/Organization")
  public Organization.Bundle organizationSearch(
      @Redact HttpServletRequest httpRequest,
      @PathVariable(value = "site") String site,
      @RequestParam(value = "type") String type,
      @RequestParam(
              value = "_count",
              required = false,
              defaultValue = "${vista-fhir-query.default-page-size}")
          int count) {
    var rpcRequest = createRpcGatewayRequestForType(type);
    if (rpcRequest == null) {
      return toBundle(httpRequest, site).empty();
    }
    var charonRequest = lighthouseRpcGatewayRequest(site, rpcRequest);
    var charonResponse = charon.request(charonRequest);
    return toBundle(httpRequest, site).apply(lighthouseRpcGatewayResponse(charonResponse));
  }

  private LhsLighthouseRpcGatewayCoverageWrite.Request organizationWriteDetails(
      CoverageWriteApi operation, Organization body) {
    var fieldsToWrite =
        R4OrganizationToInsuranceCompanyFileTransformer.builder()
            .organization(body)
            .build()
            .toInsuranceCompanyFile();
    return LhsLighthouseRpcGatewayCoverageWrite.Request.builder()
        .api(operation)
        .fields(fieldsToWrite)
        .build();
  }

  private void supportedVistaFileOrDie(String id, RecordCoordinates recordCoordinates) {
    if (!InsuranceCompany.FILE_NUMBER.equals(recordCoordinates.file())
        && !Payer.FILE_NUMBER.equals(recordCoordinates.file())) {
      throw new NotFound("Unsupported file for Organizations: " + id);
    }
  }

  private R4Bundler<
          LhsLighthouseRpcGatewayResponse, Organization, Organization.Entry, Organization.Bundle>
      toBundle(HttpServletRequest request, String vista) {
    return bundlerFactory
        .forTransformation(transformation())
        .site(vista)
        .bundling(
            R4Bundling.newBundle(Organization.Bundle::new)
                .newEntry(Organization.Entry::new)
                .build())
        .resourceType("Organization")
        .request(request)
        .build();
  }

  private R4Transformation<LhsLighthouseRpcGatewayResponse, Organization> transformation() {
    return R4Transformation.<LhsLighthouseRpcGatewayResponse, Organization>builder()
        .toResource(
            rpcResponse ->
                rpcResponse.resultsByStation().entrySet().parallelStream()
                    .flatMap(
                        rpcResults ->
                            Stream.concat(
                                R4OrganizationTransformer.builder()
                                    .rpcResults(rpcResults)
                                    .build()
                                    .toFhir(),
                                R4OrganizationPayerTransformer.builder()
                                    .site(rpcResults.getKey())
                                    .rpcResults(rpcResults.getValue())
                                    .build()
                                    .toFhir()))
                    .collect(toList()))
        .build();
  }

  private OrganizationWriteContext updateOrCreate(OrganizationWriteContext ctx) {
    var charonRequest =
        lighthouseRpcGatewayRequest(ctx.site(), organizationWriteDetails(ctx.mode(), ctx.body()));
    var charonResponse = charon.request(charonRequest);
    var lhsResponse = lighthouseRpcGatewayResponse(charonResponse);
    return ctx.result(lhsResponse);
  }

  @Getter
  @Setter
  private static class OrganizationWriteContext {
    private final String site;

    private final Organization body;

    private final String fileNumber;

    /** The CoverageWriteApi is used for organization writes as well. */
    private final CoverageWriteApi mode;

    private LhsLighthouseRpcGatewayResponse.FilemanEntry result;

    @Builder
    public OrganizationWriteContext(String site, Organization body, CoverageWriteApi mode) {
      this.site = site;
      this.body = body;
      this.mode = mode;
      this.fileNumber = InsuranceCompany.FILE_NUMBER;
    }

    String newResourceId() {
      return RecordCoordinates.builder()
          .site(site())
          .ien(result().ien())
          .file(fileNumber())
          .build()
          .toString();
    }

    OrganizationWriteContext result(LhsLighthouseRpcGatewayResponse response) {
      verifySiteSpecificVistaResponseOrDie(site(), response);
      var resultsForStation = response.resultsByStation().get(site());
      LhsGatewayErrorHandler.of(resultsForStation).validateResults();
      var results = resultsForStation.results();
      var filemanEntries =
          resultsForStation.results().stream()
              .filter(entry -> fileNumber.equals(entry.file()))
              .toList();
      if (filemanEntries.size() != 1) {
        throw ExpectationFailed.because("Unexpected number of results: " + results.size());
      }
      this.result = filemanEntries.get(0);
      return this;
    }
  }
}
