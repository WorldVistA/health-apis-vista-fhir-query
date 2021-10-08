package gov.va.api.health.vistafhirquery.service.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.vistafhirquery.service.controller.organization.OrganizationPayerSamples.VistaLhsLighthouseRpcGateway;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import org.junit.jupiter.api.Test;

public class R4OrganizationPayerTransformerTest {

  private R4OrganizationPayerTransformer _transformer() {
    return R4OrganizationPayerTransformer.builder()
        .site("888")
        .rpcResults(VistaLhsLighthouseRpcGateway.create().getsManifestResults("8"))
        .build();
  }

  @Test
  void empty() {
    var transformer =
        R4OrganizationPayerTransformer.builder()
            .site("888")
            .rpcResults(Results.builder().build())
            .build();
    assertThat(transformer.toFhir()).isEmpty();
  }

  @Test
  void toFhir() {
    assertThat(_transformer().toFhir())
        .containsExactlyInAnyOrder(OrganizationPayerSamples.R4.create().organization("888", "8"));
  }
}
