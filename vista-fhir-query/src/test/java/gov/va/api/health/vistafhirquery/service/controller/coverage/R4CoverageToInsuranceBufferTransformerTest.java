package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class R4CoverageToInsuranceBufferTransformerTest {
  private R4CoverageToInsuranceBufferTransformer _transformer() {
    return R4CoverageToInsuranceBufferTransformer.builder()
        .coverage(CoverageSamples.R4.create().coverageInsuranceBufferRead("p1", "123", "c1"))
        .build();
  }

  @Test
  void empty() {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                R4CoverageToInsuranceBufferTransformer.builder()
                    .coverage(Coverage.builder().build())
                    .build()
                    .toInsuranceBuffer());
  }

  @Test
  void inqServiceTypeCode1() {
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .inqServiceTypeCode1(
                        CodeableConcept.builder()
                            .coding(List.of(Coding.builder().build(), Coding.builder().build()))
                            .build()));
  }

  @Test
  void patientId() {
    // Null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().patientId(null));
    // Not MB
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .patientId(
                        Reference.builder()
                            .identifier(
                                Identifier.builder()
                                    .type(
                                        CodeableConcept.builder()
                                            .coding(
                                                List.of(Coding.builder().code("NOT_MB").build()))
                                            .build())
                                    .build())
                            .build()));
    // Null Identifier Value
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .patientId(
                        Reference.builder()
                            .identifier(
                                Identifier.builder()
                                    .type(
                                        CodeableConcept.builder()
                                            .coding(List.of(Coding.builder().code("MB").build()))
                                            .build())
                                    .build())
                            .build()));
  }

  @Test
  void patientRelationshipHipaa() {
    // Null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().patientRelationshipHipaa(null));
    // Too Many Codes
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .patientRelationshipHipaa(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .system(SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM)
                                        .code("spouse")
                                        .build(),
                                    Coding.builder()
                                        .system(SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM)
                                        .code("other")
                                        .build()))
                            .build()));
    // Not a valid code
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .patientRelationshipHipaa(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .system(SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM)
                                        .code("NOPE")
                                        .build()))
                            .build()));
  }

  /* filtering out the date fields because they use current date*/
  @Test
  void toInsuranceBuffer() {
    var expected =
        CoverageSamples.VistaLhsLighthouseRpcGateway.create().createInsuranceBufferInput();
    assertThat(_transformer().toInsuranceBuffer())
        .filteredOn(
            w ->
                !Set.of(
                        InsuranceVerificationProcessor.DATE_ENTERED,
                        InsuranceVerificationProcessor.SERVICE_DATE)
                    .contains(w.field()))
        .containsExactlyInAnyOrderElementsOf(expected);
  }
}
