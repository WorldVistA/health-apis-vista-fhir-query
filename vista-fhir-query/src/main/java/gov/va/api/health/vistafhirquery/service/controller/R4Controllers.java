package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static java.lang.String.join;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

@Slf4j
public class R4Controllers {

  /** Clear the ID field used when creating new resources. */
  public static void unsetIdForCreate(IsResource resource) {
    resource.id(null);
  }

  /** Set the Location header and response status to 201. */
  public static void updateResponseForCreatedResource(
      HttpServletResponse response, String newResourceUrl) {
    log.info("Resource created: {}", newResourceUrl);
    response.addHeader(HttpHeaders.LOCATION, newResourceUrl);
    response.setStatus(201);
  }

  /** Set the status to 200. */
  public static void updateResponseForUpdatedResource(
      HttpServletResponse response, String publicId) {
    log.info("Resource updated: {}", sanitize(publicId));
    response.setStatus(200);
  }

  /** Verifies that a list of resources has only one result and returns that result. */
  public static <R> R verifyAndGetResult(List<R> resources, String publicId) {
    if (resources == null) {
      throw NotFound.because(publicId);
    }
    if (resources.size() > 1) {
      throw ExpectationFailed.because(
          "Too many results returned. Expected 1 but found %d.", resources.size());
    }
    return resources.stream().findFirst().orElseThrow(() -> NotFound.because(publicId));
  }

  /**
   * Verifies that results from a site specific vista call only returns a single result for the
   * requested site.
   */
  public static void verifySiteSpecificVistaResponseOrDie(
      String site, LhsLighthouseRpcGatewayResponse response) {
    if (response.resultsByStation().size() != 1) {
      throw ExpectationFailed.because(
          "Unexpected number of vista results returned: Size(%d) Sites(%s)",
          response.resultsByStation().size(), join(",", response.resultsByStation().keySet()));
    }
    if (response.resultsByStation().get(site) == null) {
      throw ExpectationFailed.because(
          "Vista results do not contain requested site: expected %s, got %s",
          site, join(",", response.resultsByStation().keySet()));
    }
  }

  /** Indicates a critical failure in server that the user cannot solve. */
  public static class FatalServerError extends RuntimeException {
    public FatalServerError(String message) {
      super(message);
    }
  }
}
