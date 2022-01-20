package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.vistafhirquery.service.charonclient.CharonClient;
import gov.va.api.health.vistafhirquery.service.config.LinkProperties;
import gov.va.api.health.vistafhirquery.service.controller.MockWitnessProtection;
import gov.va.api.health.vistafhirquery.service.controller.R4BundlerFactory;
import gov.va.api.health.vistafhirquery.service.controller.witnessprotection.AlternatePatientIds;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class R4SiteInsuranceBufferCoverageControllerTest {
  @Mock CharonClient mockCharon;

  MockWitnessProtection mockWitnessProtection;

  private R4BundlerFactory _bundlerFactory() {
    return R4BundlerFactory.builder()
        .linkProperties(
            LinkProperties.builder()
                .defaultPageSize(15)
                .maxPageSize(100)
                .publicUrl("http://fugazi.com")
                .publicR4BasePath("hcs/{site}/r4")
                .build())
        .alternatePatientIds(new AlternatePatientIds.DisabledAlternatePatientIds())
        .build();
  }

  private R4SiteInsuranceBufferCoverageController _insuranceBufferCoverageController() {
    return R4SiteInsuranceBufferCoverageController.builder()
        .bundlerFactory(_bundlerFactory())
        .build();
  }

  private R4SiteCoverageController _insuranceTypeCoverageController() {
    return R4SiteCoverageController.builder()
        .bundlerFactory(_bundlerFactory())
        .charon(mockCharon)
        .witnessProtection(mockWitnessProtection)
        .build();
  }

  @Test
  void coverageCreateNoHack() {
    var response = new MockHttpServletResponse();
    var coverageSample = CoverageSamples.R4.create().coverage();
    _insuranceBufferCoverageController().coverageCreate(response, "123", coverageSample);
    assertThat(response.getHeader(HttpHeaders.LOCATION))
        .isEqualTo("http://fugazi.com/hcs/123/r4/Coverage/not-available");
  }

  @Test
  void coverageCreateUsingInsuranceTypeControllerHack() {
    var response = new MockHttpServletResponse();
    var coverageSample = CoverageSamples.R4.create().coverage();
    _insuranceTypeCoverageController().coverageCreate(response, "123", true, coverageSample);
    assertThat(response.getHeader(HttpHeaders.LOCATION))
        .isEqualTo("http://fugazi.com/hcs/123/r4/Coverage/not-available");
  }
}
