package gov.va.api.health.vistafhirquery.service.controller.raw;

import static gov.va.api.health.vistafhirquery.service.charonclient.CharonRequests.lighthouseRpcGatewayRequest;

import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.controller.coverage.R4SiteCoverageController;
import gov.va.api.health.vistafhirquery.service.controller.organization.R4SiteOrganizationController;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.WitnessProtection;
import gov.va.api.lighthouse.charon.api.v1.RpcInvocationResultV1;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RawController {
  private final CharonClient charon;
  private final WitnessProtection witnessProtection;

  /** Get the raw response that the coverage controller transforms to fhir. */
  @GetMapping(
      value = "/Coverage",
      params = {"site", "icn"})
  public RpcInvocationResultV1 coverageBySiteAndIcn(
      @RequestParam(name = "site") String site, @RequestParam(name = "icn") String icn) {
    return makeRequest(site, R4SiteCoverageController.coverageByPatientIcn(icn));
  }

  private <I extends TypeSafeRpcRequest> RpcInvocationResultV1 makeRequest(String site, I request) {
    try {
      return charon.request(lighthouseRpcGatewayRequest(site, request)).invocationResult();
    } catch (Exception e) {
      log.error("Failed", e);
      throw e;
    }
  }

  /** Raw organization read. */
  @GetMapping(
      value = {"/Organization"},
      params = {"id"})
  public @NonNull RpcInvocationResultV1 organizationById(@RequestParam(name = "id") String id) {
    var coordinates = witnessProtection.toRecordCoordinates(id);
    return makeRequest(
        coordinates.site(), R4SiteOrganizationController.manifestRequest(coordinates));
  }
}
