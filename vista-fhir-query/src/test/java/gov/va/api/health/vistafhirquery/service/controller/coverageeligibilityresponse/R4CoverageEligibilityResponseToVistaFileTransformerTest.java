package gov.va.api.health.vistafhirquery.service.controller.coverageeligibilityresponse;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Money;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Outcome;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Purpose;
import gov.va.api.health.r4.api.resources.CoverageEligibilityResponse.Status;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExactlyOneOfFields;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
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
  void extractFromCodeableConceptOrDie() {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer().extractFromCodeableConceptOrDie(".json", "1", "1", "system", null));
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .extractFromCodeableConceptOrDie(
                        ".json", "1", "1", "system", CodeableConcept.builder().build()));
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .extractFromCodeableConceptOrDie(
                        ".json",
                        "1",
                        "1",
                        "system",
                        CodeableConcept.builder()
                            .coding(List.of(Coding.builder().build(), Coding.builder().build()))
                            .build()));
  }

  @Test
  void invalidDateTimePeriodThrows() {
    // Period is null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().dateTimePeriod(null));
    // Start is not populated
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () -> _transformer().dateTimePeriod(Period.builder().end("2021-08-08").build()));
  }

  @Test
  void invalidIdentifierThrows() {
    // identifier.type is null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().identifier(Identifier.builder().build()));
    // identifier.type.text is null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .identifier(
                        Identifier.builder().type(CodeableConcept.builder().build()).build()));
    // identifier type is unknown
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .identifier(
                        Identifier.builder()
                            .type(CodeableConcept.builder().text("WHO-IS-SHE?").build())
                            .build()));
    // identifier exists but value is null
    assertThatExceptionOfType(RequestPayloadExceptions.InvalidConditionalField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .identifier(
                        Identifier.builder()
                            .type(CodeableConcept.builder().text("MSH-10").build())
                            .build()));
    assertThatExceptionOfType(RequestPayloadExceptions.InvalidConditionalField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .identifier(
                        Identifier.builder()
                            .type(CodeableConcept.builder().text("MSA-3").build())
                            .build()));
  }

  @Test
  void invalidMoneyThrows() {
    // Both null
    assertThatExceptionOfType(ExactlyOneOfFields.class)
        .isThrownBy(() -> _transformer().money(null, null));
    // Both non-null
    assertThatExceptionOfType(ExactlyOneOfFields.class)
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
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().procedureCoding(null));
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().procedureCoding(CodeableConcept.builder().build()));
    // Too many codings
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
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
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().procedureModifier(null));
    // Too manies
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .procedureModifier(
                        List.of(
                            CodeableConcept.builder().build(), CodeableConcept.builder().build())));
  }

  @Test
  void outcome() {
    var cerWithBadOutcome = _transformer().coverageEligibilityResponse().outcome(Outcome.error);
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(
            () ->
                R4CoverageEligibilityResponseToVistaFileTransformer.builder()
                    .coverageEligibilityResponse(cerWithBadOutcome)
                    .build()
                    .outcome());
  }

  @Test
  void purpose() {
    var cerWithEmptyPurpose = _transformer().coverageEligibilityResponse().purpose(emptyList());
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(
            () ->
                R4CoverageEligibilityResponseToVistaFileTransformer.builder()
                    .coverageEligibilityResponse(cerWithEmptyPurpose)
                    .build()
                    .purpose());
    var cerWithBadPurpose =
        _transformer().coverageEligibilityResponse().purpose(List.of(Purpose.validation));
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(
            () ->
                R4CoverageEligibilityResponseToVistaFileTransformer.builder()
                    .coverageEligibilityResponse(cerWithBadPurpose)
                    .build()
                    .purpose());
  }

  @Test
  void status() {
    var cerWithBadStatus = _transformer().coverageEligibilityResponse().status(Status.cancelled);
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(
            () ->
                R4CoverageEligibilityResponseToVistaFileTransformer.builder()
                    .coverageEligibilityResponse(cerWithBadStatus)
                    .build()
                    .status());
  }

  @Test
  void toVistaFile() {
    var samples = CoverageEligibilityResponseSamples.VistaLhsLighthouseRpcGateway.create();
    assertThat(_transformer().toVistaFiles())
        .containsExactlyInAnyOrderElementsOf(
            Stream.of(
                    samples.eligibilityBenefitFilemanValues(),
                    samples.healthCareCodeInformationFilemanValues(),
                    samples.healthcareServicesDeliveryFilemanValues(),
                    samples.ienMacroPointers(),
                    samples.iivResponseFilemanValues(),
                    samples.insuranceTypeFilemanValue(),
                    samples.serviceTypesFilemanValues(),
                    samples.subscriberAdditionalInfoFilemanValues(),
                    samples.subscriberDatesFilemanValues(),
                    samples.subscriberReferenceIdFilemanValues())
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
