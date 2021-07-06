package gov.va.api.health.vistafhirquery.service.controller.raw;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.vistafhirquery.service.controller.VistalinkApiClient;
import gov.va.api.lighthouse.charon.api.RpcPrincipal;
import gov.va.api.lighthouse.charon.api.RpcRequest;
import gov.va.api.lighthouse.charon.api.RpcResponse;
import gov.va.api.lighthouse.charon.api.RpcVistaTargets;
import gov.va.api.lighthouse.charon.models.iblhsamcmsgetins.IblhsAmcmsGetIns;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayGetsManifest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Internal endpoint for getting raw payloads directly from vista. */
@Validated
@RestController
@RequestMapping(
    value = "/internal/raw",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = {@Autowired, @NonNull})
@Builder
public class RawController {
  private final VistalinkApiClient vistalinkApiClient;

  /** Get the raw response that the coverage controller transforms to fhir. */
  @GetMapping(
      value = "/Coverage",
      params = {"site", "icn"})
  public RpcResponse rawCoverageResponse(
      @RequestParam(name = "site") String site,
      @RequestParam(name = "icn") String icn,
      @Redact @RequestParam(name = "accessCode", required = false) String accessCode,
      @Redact @RequestParam(name = "verifyCode", required = false) String verifyCode,
      @Redact @RequestParam(name = "apu", required = false) String apu) {
    // ToDo dfn macro?
    LhsLighthouseRpcGatewayGetsManifest.Request lhsGetsManifestRequest =
        LhsLighthouseRpcGatewayGetsManifest.Request.builder()
            .file("2")
            .iens(icn)
            .fields(List.of(".3121*"))
            .flags(
                List.of(
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags.OMIT_NULL_VALUES,
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags
                        .RETURN_INTERNAL_VALUES,
                    LhsLighthouseRpcGatewayGetsManifest.Request.GetsManifestFlags
                        .RETURN_EXTERNAL_VALUES))
            .build();
    if (isBlank(accessCode) || isBlank(verifyCode) || isBlank(apu)) {
      return vistalinkApiClient.requestForVistaSite(site, lhsGetsManifestRequest);
    }
    return vistalinkApiClient.makeRequest(
        RpcRequest.builder()
            .principal(
                RpcPrincipal.builder()
                    .accessCode(accessCode)
                    .verifyCode(verifyCode)
                    .applicationProxyUser(apu)
                    .build())
            .target(RpcVistaTargets.builder().include(List.of(site)).build())
            .rpc(lhsGetsManifestRequest.asDetails())
            .build());
  }

  /** Get the raw data. */
  @GetMapping(
      value = {"/Organization"},
      params = {"site", "icn"})
  public RpcResponse rawResponse(
      @RequestParam(name = "site") String site,
      @RequestParam(name = "icn") String icn,
      @Redact @RequestParam(name = "accessCode", required = false) String accessCode,
      @Redact @RequestParam(name = "verifyCode", required = false) String verifyCode,
      @Redact @RequestParam(name = "apu", required = false) String apu) {
    if (isBlank(accessCode) || isBlank(verifyCode) || isBlank(apu)) {
      return vistalinkApiClient.requestForVistaSite(
          site, IblhsAmcmsGetIns.Request.builder().icn(icn).build());
    }
    return vistalinkApiClient.makeRequest(
        RpcRequest.builder()
            .principal(
                RpcPrincipal.builder()
                    .accessCode(accessCode)
                    .verifyCode(verifyCode)
                    .applicationProxyUser(apu)
                    .build())
            .target(RpcVistaTargets.builder().include(List.of(site)).build())
            .rpc(IblhsAmcmsGetIns.Request.builder().icn(icn).build().asDetails())
            .build());
  }
}
