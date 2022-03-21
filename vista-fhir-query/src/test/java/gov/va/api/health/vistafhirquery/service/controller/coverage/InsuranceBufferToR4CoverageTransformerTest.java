package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.RelatedPerson;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InsuranceBufferToR4CoverageTransformerTest {
  private InsuranceBufferToR4CoverageTransformer _transformer(Results results) {
    return InsuranceBufferToR4CoverageTransformer.builder()
        .patientIcn("p1")
        .site("123")
        .results(results)
        .build();
  }

  private InsuranceBufferToR4CoverageTransformer _transformer() {
    return _transformer(
        CoverageSamples.VistaLhsLighthouseRpcGateway.create()
            .createInsuranceBufferResults("ien1", SubscriberToBeneficiaryRelationship.SPOUSE));
  }

  @Test
  void birthsex() {
    assertThat(_transformer().birthsex(FilemanEntry.builder().build()))
        .isEqualTo(
            Extension.builder()
                .url(InsuranceBufferDefinitions.get().insuredsSex().structureDefinition())
                .valueCode("UNK")
                .build());
    assertThat(
            _transformer()
                .birthsex(
                    FilemanEntry.builder()
                        .fields(
                            Map.of(
                                InsuranceVerificationProcessor.INSUREDS_SEX,
                                LhsLighthouseRpcGatewayResponse.Values.builder()
                                    .ext("M")
                                    .in("M")
                                    .build()))
                        .build()))
        .isEqualTo(
            Extension.builder()
                .url(InsuranceBufferDefinitions.get().insuredsSex().structureDefinition())
                .valueCode("M")
                .build());
  }

  private boolean coverageHasContainedRelatedPersonResource(Coverage coverage) {
    return coverage.contained().stream()
        .anyMatch(contained -> RelatedPerson.class.equals(contained.getClass()));
  }

  @Test
  void empty() {
    assertThat(_transformer(Results.builder().build()).toFhir()).isEmpty();
    assertThat(_transformer(Results.builder().results(List.of()).build()).toFhir()).isEmpty();
    assertThat(
            _transformer(Results.builder().results(List.of(FilemanEntry.builder().build())).build())
                .toFhir())
        .isEmpty();
    assertThat(
            _transformer(
                    Results.builder()
                        .results(List.of(FilemanEntry.builder().fields(Map.of()).build()))
                        .build())
                .toFhir())
        .isEmpty();
  }

  @Test
  void relatedPersonResourceIsOmittedWhenRelationshipIsSelf() {
    var actual =
        _transformer(
                CoverageSamples.VistaLhsLighthouseRpcGateway.create()
                    .createInsuranceBufferResults(
                        "ien-1", SubscriberToBeneficiaryRelationship.SELF))
            .toFhir();
    assertThat(actual).noneMatch(this::coverageHasContainedRelatedPersonResource);
  }

  @Test
  void toFhir() {
    var expected = CoverageSamples.R4.create().coverageInsuranceBufferRead("p1", "123", "ien1");
    CoverageSamples.R4.cleanUpContainedReferencesForComparison(expected);
    var actual =
        _transformer()
            .toFhir()
            .peek(CoverageSamples.R4::cleanUpContainedReferencesForComparison)
            .toList();
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected.asList());
    assertThat(actual).allSatisfy(this::coverageHasContainedRelatedPersonResource);
  }
}
