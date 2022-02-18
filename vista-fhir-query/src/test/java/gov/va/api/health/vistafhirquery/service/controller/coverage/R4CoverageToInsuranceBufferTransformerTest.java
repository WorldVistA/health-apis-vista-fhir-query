package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidStringLengthInclusively;
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
  void effectiveDateAndExpirationDate() {
    // Null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().effectiveAndExpirationDate(null));
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(() -> _transformer().effectiveAndExpirationDate(Period.builder().build()));
    // Bad start date
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .effectiveAndExpirationDate(Period.builder().start("badDate").build()));
    // Bad end date
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .effectiveAndExpirationDate(
                        Period.builder().start("1993-01-12T00:00:00Z").end("badDate").build()));
    // End date before start date
    assertThatExceptionOfType(RequestPayloadExceptions.EndDateOccursBeforeStartDate.class)
        .isThrownBy(
            () ->
                _transformer()
                    .effectiveAndExpirationDate(
                        Period.builder()
                            .start("2025-01-01T00:00:00Z")
                            .end("1993-01-12T00:00:00Z")
                            .build()));
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
  void groupNumber() {
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(() -> _transformer().groupNumber(null));
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(() -> _transformer().groupNumber(List.of()));
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .groupNumber(
                        Identifier.builder()
                            .system(InsuranceBufferStructureDefinitions.GROUP_NUMBER)
                            .build()
                            .asList()));
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
  void insuranceCompanyName() {
    // Null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().insuranceCompanyName(null));
    // Less than 3 Characters
    assertThatExceptionOfType(InvalidStringLengthInclusively.class)
        .isThrownBy(() -> _transformer().insuranceCompanyName("NO"));
    // More than 30 Characters
    assertThatExceptionOfType(InvalidStringLengthInclusively.class)
        .isThrownBy(
            () ->
                _transformer()
                    .insuranceCompanyName("0123456789101112131415161718192021222324252627282930"));
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

  @Test
  void subscriberId() {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().subscriberId(null));
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

  @Test
  void typeOfPlan() {
    // Null plan
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().typeOfPlan(null));
    // Null codeableconcept
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().typeOfPlan(InsurancePlan.Plan.builder().build().asList()));
    // Null coding
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .typeOfPlan(
                        InsurancePlan.Plan.builder()
                            .type(CodeableConcept.builder().build())
                            .build()
                            .asList()));
    // Too many plans
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .typeOfPlan(
                        List.of(
                            InsurancePlan.Plan.builder().build(),
                            InsurancePlan.Plan.builder().build())));
    // Too many codings
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .typeOfPlan(
                        InsurancePlan.Plan.builder()
                            .type(
                                CodeableConcept.builder()
                                    .coding(
                                        List.of(Coding.builder().build(), Coding.builder().build()))
                                    .build())
                            .build()
                            .asList()));
  }
}
