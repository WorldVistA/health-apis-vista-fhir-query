package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.stream.Stream;
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
    var samples = CoverageEligibilityResponseSamples.VistaLhsLighthouseRpcGateway.create();
    assertThat(_transformer().toVistaFiles())
        .containsExactlyInAnyOrderElementsOf(
            Stream.of(samples.serviceTypesFilemanValues(), samples.subscriberDatesFilemanValues())
                .flatMap(Collection::stream)
                .toList());
  }
}
