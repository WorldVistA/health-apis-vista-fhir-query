package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.updateResponseForCreatedResource;

import gov.va.api.health.autoconfig.logging.Redact;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
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

  /** Create Support. */
  public void coverageCreate(
      @Redact HttpServletResponse response, String site, @Redact Coverage body) {
    updateResponseForCreatedResource(
        response, bundlerFactory.linkProperties().r4().readUrl(site, "Coverage", "not-available"));
  }
}
