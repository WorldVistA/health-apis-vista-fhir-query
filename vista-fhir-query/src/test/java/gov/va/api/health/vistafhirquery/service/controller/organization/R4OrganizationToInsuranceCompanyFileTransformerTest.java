package gov.va.api.health.vistafhirquery.service.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import java.util.List;
import org.junit.jupiter.api.Test;

public class R4OrganizationToInsuranceCompanyFileTransformerTest {
  private R4OrganizationToInsuranceCompanyFileTransformer _transformer() {
    return R4OrganizationToInsuranceCompanyFileTransformer.builder()
        .organization(OrganizationSamples.R4.create().organization())
        .include277EdiNumber(true)
        .build();
  }

  @Test
  void contactPointThrowsBadRequestPayload() {
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> _transformer().contactPoint(null, null, null, null));
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () ->
                _transformer()
                    .contactPoint(
                        List.of(ContactPoint.builder().build()),
                        null,
                        null,
                        ContactPoint.ContactPointSystem.url));
  }

  @Test
  void empty() {
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () ->
                R4OrganizationToInsuranceCompanyFileTransformer.builder()
                    .organization(Organization.builder().build())
                    .build()
                    .toInsuranceCompanyFile());
  }

  @Test
  void forSystemReturnEmptyOnNull() {
    _transformer().extensionForSystem(null, null).isEmpty();
  }

  @Test
  void toInsuranceCompanyFile() {
    var expected = OrganizationSamples.VistaLhsLighthouseRpcGateway.create().createApiInput();
    assertThat(_transformer().toInsuranceCompanyFile())
        .containsExactlyInAnyOrderElementsOf(expected);
  }
}
