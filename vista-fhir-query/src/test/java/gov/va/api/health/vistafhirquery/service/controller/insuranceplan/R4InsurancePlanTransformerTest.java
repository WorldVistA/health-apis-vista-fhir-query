package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static gov.va.api.health.vistafhirquery.service.controller.insuranceplan.InsurancePlanSamples.json;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class R4InsurancePlanTransformerTest {

  @Test
  void empty() {
    assertThat(
            R4InsurancePlanTransformer.builder()
                .rpcResults(
                    Map.entry("666", LhsLighthouseRpcGatewayResponse.Results.builder().build()))
                .build()
                .toFhir())
        .isEmpty();
  }

  @Test
  void toFhir() {
    assertThat(
            json(
                R4InsurancePlanTransformer.builder()
                    .rpcResults(
                        Map.entry(
                            "666",
                            InsurancePlanSamples.VistaLhsLighthouseRpcGateway.create()
                                .getsManifestResults()))
                    .build()
                    .toFhir()
                    .findFirst()
                    .get()))
        .isEqualTo(json(InsurancePlanSamples.R4.create().insurancePlan()));
  }
}
