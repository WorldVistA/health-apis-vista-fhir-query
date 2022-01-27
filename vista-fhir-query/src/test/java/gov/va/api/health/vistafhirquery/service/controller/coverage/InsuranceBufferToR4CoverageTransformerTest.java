package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.Status;
import org.junit.jupiter.api.Test;

class InsuranceBufferToR4CoverageTransformerTest {

  private InsuranceBufferToR4CoverageTransformer _transformer() {
    return InsuranceBufferToR4CoverageTransformer.builder()
        .site("123")
        .results(
            CoverageSamples.VistaLhsLighthouseRpcGateway.create()
                .createInsuranceBufferResults("ien1"))
        .build();
  }

  @Test
  void toFhir() {
    assertThat(_transformer().toFhir())
        .containsOnly(Coverage.builder().id("shanktopus").status(Status.draft).build());
  }
}
