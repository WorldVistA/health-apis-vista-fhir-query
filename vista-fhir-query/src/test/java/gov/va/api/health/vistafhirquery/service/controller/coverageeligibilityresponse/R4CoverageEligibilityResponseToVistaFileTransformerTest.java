package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class R4CoverageEligibilityResponseToVistaFileTransformerTest {

  private R4CoverageEligibilityResponseToVistaFileTransformer _transformer() {
    return R4CoverageEligibilityResponseToVistaFileTransformer.builder()
        .coverageEligibilityResponse(
            CoverageEligibilityResponseSamples.R4.create().coverageEligibilityResponseForWrite())
        .build();
  }

  @Test
  void toVistaFile() {
    assertThat(_transformer().toVistaFiles())
        .containsExactlyInAnyOrderElementsOf(
            CoverageEligibilityResponseSamples.VistaLhsLighthouseRpcGateway.create()
                .subscriberDatesFilemanValues());
  }
}
