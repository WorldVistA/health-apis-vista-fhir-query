package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.parseOrDie;
import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.api.R4CoverageApi;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundler;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.R4Bundling;
import gov.va.api.health.vistafhirquery.service.controller.R4Transformation;
import gov.va.api.health.vistafhirquery.service.controller.SegmentedVistaIdentifier;
import gov.va.api.health.vistafhirquery.service.controller.VistalinkApiClient;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.api.RpcResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Builder
@Validated
@RestController
@RequestMapping(
    value = "/r4/Coverage",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
public class R4CoverageController implements R4CoverageApi {
  private final R4BundlerFactory bundlerFactory;

  private final VistalinkApiClient vistalinkApiClient;

  private final WitnessProtection witnessProtection;

  /** Uses the metadata in the RpcResponse to map sites to timezones. */
  private Map<String, ZoneId> collectTimezones(RpcResponse rpcResponse) {
    return rpcResponse.results().stream()
        .filter(r -> r.metadata() != null)
        .collect(toMap(r -> r.vista(), r -> ZoneId.of(r.metadata().timezone())));
  }

  @Override
  @GetMapping(value = "/{publicId}")
  public Coverage coverageRead(@PathVariable(value = "publicId") String id) {
    SegmentedVistaIdentifier svi = parseOrDie(witnessProtection, id);
    LhsLighthouseRpcGatewayGetsManifest.Request rpcRequest =
        LhsLighthouseRpcGatewayGetsManifest.Request.builder()
            .file("2.312")
            .iens(svi.vistaRecordId())
            .fields(List.of(".01", ".18", ".2", "3", "3.04", "4.03", "4.06", "7.02", "8"))
            .flags(
                List.of(
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags.OMIT_NULL_VALUES,
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags
                        .RETURN_INTERNAL_VALUES,
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags
                        .RETURN_EXTERNAL_VALUES))
            .build();
    RpcResponse rpcResponse = vistalinkApiClient.requestForVistaSite(svi.vistaSiteId(), rpcRequest);
    Map<String, ZoneId> vistaZoneIds = collectTimezones(rpcResponse);
    LhsLighthouseRpcGatewayResponse getsManifestResults =
        LhsLighthouseRpcGatewayGetsManifest.create().fromResults(rpcResponse.results());
    List<Coverage> resources =
        transformation(vistaZoneIds, svi.patientIdentifier())
            .toResource()
            .apply(getsManifestResults);
    return verifyAndGetResult(resources, id);
  }

  /** Search support. */
  @Override
  @GetMapping
  public Coverage.Bundle coverageSearch(
      HttpServletRequest request,
      @RequestHeader(value = "coverageHack", required = false, defaultValue = "false")
          String coverageHack,
      @RequestParam(value = "patient") String patient,
      @RequestParam(value = "_count", required = false) Integer count) {
    if (CoverageHack.isEnabled(coverageHack)) {
      patient = CoverageHack.dfn();
    }
    // ToDo dfn macro on the iens field
    LhsLighthouseRpcGatewayGetsManifest.Request rpcRequest =
        LhsLighthouseRpcGatewayGetsManifest.Request.builder()
            .file("2")
            .iens(patient)
            .fields(List.of(".3121*"))
            .flags(
                List.of(
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags.OMIT_NULL_VALUES,
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags
                        .RETURN_INTERNAL_VALUES,
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags
                        .RETURN_EXTERNAL_VALUES))
            .build();
    RpcResponse rpcResponse = vistalinkApiClient.requestForPatient(patient, rpcRequest);
    Map<String, ZoneId> vistaZoneIds = collectTimezones(rpcResponse);
    LhsLighthouseRpcGatewayResponse getsManifestResults =
        LhsLighthouseRpcGatewayGetsManifest.create().fromResults(rpcResponse.results());
    return toBundle(request, vistaZoneIds).apply(getsManifestResults);
  }

  private R4Bundler<LhsLighthouseRpcGatewayResponse, Coverage, Coverage.Entry, Coverage.Bundle>
      toBundle(HttpServletRequest request, Map<String, ZoneId> vistaZoneIds) {
    return bundlerFactory
        .forTransformation(transformation(vistaZoneIds, request.getParameter("patient")))
        .bundling(R4Bundling.newBundle(Coverage.Bundle::new).newEntry(Coverage.Entry::new).build())
        .resourceType("Coverage")
        .request(request)
        .build();
  }

  private R4Transformation<LhsLighthouseRpcGatewayResponse, Coverage> transformation(
      Map<String, ZoneId> vistaZoneIds, String patientId) {
    return R4Transformation.<LhsLighthouseRpcGatewayResponse, Coverage>builder()
        .toResource(
            rpcResponse ->
                rpcResponse.resultsByStation().entrySet().parallelStream()
                    .flatMap(
                        rpcResults ->
                            R4CoverageTransformer.builder()
                                .patientIcn(patientId)
                                .vistaZoneId(
                                    vistaZoneIds.getOrDefault(rpcResults.getKey(), ZoneOffset.UTC))
                                .rpcResults(rpcResults)
                                .build()
                                .toFhir())
                    .collect(toList()))
        .build();
  }

  static class CoverageHack {
    static String dfn() {
      return "69";
    }

    static boolean isEnabled(String header) {
      return StringUtils.equals(header, "true");
    }
  }
}
