package gov.va.api.health.vistafhirquery.service.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class R4OrganizationToInsuranceCompanyFileTransformerTest {
  private R4OrganizationToInsuranceCompanyFileTransformer _transformer() {
    return R4OrganizationToInsuranceCompanyFileTransformer.builder()
        .organization(OrganizationSamples.R4.create().organization())
        .build();
  }

  // TODO:  https://vajira.max.gov/browse/API-10379
  // FUN
  @Disabled
  @Test
  void toInsuranceCompanyFile() {
    var expected = OrganizationSamples.VistaLhsLighthouseRpcGateway.create().createApiInput();
    assertThat(_transformer().toInsuranceCompanyFile())
        .containsExactlyInAnyOrderElementsOf(expected);
  }
}
