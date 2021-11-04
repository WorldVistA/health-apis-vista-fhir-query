package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Money;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class R4CoverageEligibilityResponseToVistaFileTransformerTest {
  private R4CoverageEligibilityResponseToVistaFileTransformer _transformer() {
    return R4CoverageEligibilityResponseToVistaFileTransformer.builder()
        .coverageEligibilityResponse(
            CoverageEligibilityResponseSamples.R4.create().coverageEligibilityResponseForWrite())
        .build();
  }

  @Test
  void invalidDateTimePeriodThrows() {
    // Period is null
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> _transformer().dateTimePeriod(null));
    // Start is not populated
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () -> _transformer().dateTimePeriod(Period.builder().end("2021-08-08").build()));
  }

  @Test
  void invalidIdentifierThrows() {
    // identifier.type is null
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> _transformer().identifier(Identifier.builder().build()));
    // identifier.type.text is null
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () ->
                _transformer()
                    .identifier(
                        Identifier.builder().type(CodeableConcept.builder().build()).build()));
    // identifier type is unknown
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                _transformer()
                    .identifier(
                        Identifier.builder()
                            .type(CodeableConcept.builder().text("WHO-IS-SHE?").build())
                            .build()));
  }

  @Test
  void invalidMoneyThrows() {
    // Both null
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> _transformer().money(null, null));
    // Both non-null
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () ->
                _transformer()
                    .money(
                        Money.builder().value(new BigDecimal("88.88")).build(),
                        Money.builder().value(new BigDecimal("88.88")).build()));
  }

  @Test
  void invalidProcedureCodingThrows() {
    // Null checks
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> _transformer().procedureCoding(null));
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> _transformer().procedureCoding(CodeableConcept.builder().build()));
    // Too many codings
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () ->
                _transformer()
                    .procedureCoding(
                        CodeableConcept.builder()
                            .coding(List.of(Coding.builder().build(), Coding.builder().build()))
                            .build()));
  }

  @Test
  void invalidProcedureModifierThrows() {
    // Null
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> _transformer().procedureModifier(null));
    // Too manies
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () ->
                _transformer()
                    .procedureModifier(
                        List.of(
                            CodeableConcept.builder().build(), CodeableConcept.builder().build())));
  }

  @Test
  void toVistaFile() {
    var samples = CoverageEligibilityResponseSamples.VistaLhsLighthouseRpcGateway.create();
    assertThat(_transformer().toVistaFiles())
        .containsExactlyInAnyOrderElementsOf(
            Stream.of(
                    samples.eligibilityBenefitFilemanValues(),
                    samples.healthCareCodeInformationFilemanValues(),
                    samples.ienMacroPointers(),
                    samples.iivResponseFilemanValues(),
                    samples.serviceTypesFilemanValues(),
                    samples.subscriberDatesFilemanValues())
                .flatMap(Collection::stream)
                .toList());
  }

  @ParameterizedTest
  @CsvSource(
      nullValues = "null",
      value = {"null,U", "true,Y", "false,N"})
  void x12YesNo(Boolean sample, String expected) {
    assertThat(_transformer().x12YesNo(sample)).isEqualTo(expected);
  }
}
