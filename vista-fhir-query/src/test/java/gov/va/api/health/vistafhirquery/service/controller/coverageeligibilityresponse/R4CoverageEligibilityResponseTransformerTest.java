package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageSamples;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import org.junit.jupiter.api.Test;

public class R4CoverageEligibilityResponseTransformerTest {
  private R4CoverageEligibilityResponseTransformer _transformer() {
    return _transformer(
        R4CoverageEligibilityResponseSearchContext.builder()
            .site("123")
            .patientIcn("p1")
            .coverageResults(
                CoverageSamples.VistaLhsLighthouseRpcGateway.create().getsManifestResults("c1"))
            .planLimitationsResults(
                CoverageEligibilityResponseSamples.VistaLhsLighthouseRpcGateway.create()
                    .getsManifestResults())
            .build());
  }

  private R4CoverageEligibilityResponseTransformer _transformer(
      R4CoverageEligibilityResponseSearchContext ctx) {
    return R4CoverageEligibilityResponseTransformer.builder().searchContext(ctx).build();
  }

  @Test
  void empty() {
    // Coverage results is empty
    var ctx =
        R4CoverageEligibilityResponseSearchContext.builder()
            .site("123")
            .patientIcn("p1")
            .coverageResults(LhsLighthouseRpcGatewayResponse.Results.builder().build())
            .planLimitationsResults(LhsLighthouseRpcGatewayResponse.Results.builder().build())
            .build();
    assertThat(_transformer(ctx).toFhir()).isEmpty();
    // Plan Limitations is empty
    ctx.coverageResults(
        CoverageSamples.VistaLhsLighthouseRpcGateway.create().getsManifestResults("c1"));
    assertThat(_transformer(ctx).toFhir()).isEmpty();
  }

  @Test
  void toFhir() {
    assertThat(_transformer().toFhir().findFirst().orElse(null))
        .usingRecursiveComparison()
        .ignoringFields("created")
        .isEqualTo(
            CoverageEligibilityResponseSamples.R4.create().coverageEligibilityResponse("p1", "4"));
  }
}
