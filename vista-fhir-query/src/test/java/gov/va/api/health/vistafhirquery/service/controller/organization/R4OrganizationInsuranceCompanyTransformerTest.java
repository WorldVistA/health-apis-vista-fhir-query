package gov.va.api.health.vistafhirquery.service.controller.organization;

import static gov.va.api.health.vistafhirquery.service.controller.organization.OrganizationSamples.R4.sortExtensions;
import static gov.va.api.health.vistafhirquery.service.controller.organization.OrganizationSamples.json;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class R4OrganizationInsuranceCompanyTransformerTest {

  @Test
  void empty() {
    assertThat(
            R4OrganizationInsuranceCompanyTransformer.builder()
                .rpcResults(
                    Map.entry("666", LhsLighthouseRpcGatewayResponse.Results.builder().build()))
                .build()
                .toFhir())
        .isEmpty();
  }

  @Test
  void toFhir() {
    Organization actual =
        R4OrganizationInsuranceCompanyTransformer.builder()
            .rpcResults(
                Map.entry(
                    "666",
                    OrganizationSamples.VistaLhsLighthouseRpcGateway.create()
                        .getsManifestResults()))
            .build()
            .toFhir()
            .findFirst()
            .get();

    assertThat(json(sortExtensions(actual)))
        .isEqualTo(json(sortExtensions(OrganizationSamples.R4.create().organization())));
  }
}
