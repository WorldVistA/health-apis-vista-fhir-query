package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

public class R4SiteCoverageEligibilityResponseControllerTest {

  private R4SiteCoverageEligibilityResponseController _controller() {
    return R4SiteCoverageEligibilityResponseController.builder()
        .bundlerFactory(
            R4BundlerFactory.builder()
                .linkProperties(
                    LinkProperties.builder()
                        .defaultPageSize(15)
                        .maxPageSize(100)
                        .publicUrl("http://fugazi.com")
                        .publicR4BasePath("site/{site}/r4")
                        .build())
                .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
                .build())
        .build();
  }

  @Test
  void noop() {
    assertThat(
            _controller()
                .coverageEligibilityResponseSearch(
                    mock(HttpServletRequest.class), "123", "p1", null)
                .entry())
        .isEmpty();
  }
}
