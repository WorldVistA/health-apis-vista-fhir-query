package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.organization.OrganizationSamples;
import gov.va.api.health.vistafhirquery.service.controller.organization.OrganizationStructureDefinitions;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import org.junit.jupiter.api.Test;

public class CodeableConceptExtensionHandlerTest {

  private final WriteableFilemanValueFactory filemanFactory =
      WriteableFilemanValueFactory.forFile(InsuranceCompany.FILE_NUMBER);

  @Test
  void handle() {
    assertThat(
            CodeableConceptExtensionHandler.forDefiningUrl(
                    OrganizationStructureDefinitions.TYPE_OF_COVERAGE)
                .filemanFactory(filemanFactory)
                .fieldNumber(InsuranceCompany.TYPE_OF_COVERAGE)
                .codingSystem(OrganizationStructureDefinitions.TYPE_OF_COVERAGE_URN_OID)
                .build()
                .handle(OrganizationSamples.R4.create().typeOfCoverage()))
        .isEqualTo(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file(InsuranceCompany.FILE_NUMBER)
                .field(InsuranceCompany.TYPE_OF_COVERAGE)
                .index(1)
                .value("HEALTH INSURANCE")
                .build());
  }
}
