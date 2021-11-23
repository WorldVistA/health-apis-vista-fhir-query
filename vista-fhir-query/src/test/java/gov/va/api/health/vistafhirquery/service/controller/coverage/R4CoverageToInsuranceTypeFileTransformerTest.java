package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.Coverage.CoverageClass;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceType;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

public class R4CoverageToInsuranceTypeFileTransformerTest {
  private R4CoverageToInsuranceTypeFileTransformer _transformer() {
    return R4CoverageToInsuranceTypeFileTransformer.builder()
        .coverage(CoverageSamples.R4.create().coverage())
        .build();
  }

  void assertBadRequestBodyThrown(ThrowingCallable r) {
    assertThatExceptionOfType(BadRequestPayload.class).isThrownBy(r);
  }

  @Test
  void coordinationOfBenefits() {
    assertBadRequestBodyThrown(() -> _transformer().coordinationOfBenefits(null));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> _transformer().coordinationOfBenefits(0));
    assertThat(_transformer().coordinationOfBenefits(1).value()).isEqualTo("PRIMARY");
    assertThat(_transformer().coordinationOfBenefits(2).value()).isEqualTo("SECONDARY");
    assertThat(_transformer().coordinationOfBenefits(3).value()).isEqualTo("TERTIARY");
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> _transformer().coordinationOfBenefits(4));
  }

  @Test
  void empty() {
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(
            () ->
                R4CoverageToInsuranceTypeFileTransformer.builder()
                    .coverage(Coverage.builder().build())
                    .build()
                    .toInsuranceTypeFile());
  }

  @Test
  void groupPlan() {
    // Null
    assertBadRequestBodyThrown(() -> _transformer().groupPlan(null));
    // Empty
    var covClass = CoverageClass.builder().build();
    assertBadRequestBodyThrown(() -> _transformer().groupPlan(List.of(covClass)));
    // Not group plan
    covClass.type(
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().code("NOT_GROUP").build()))
            .build());
    assertBadRequestBodyThrown(() -> _transformer().groupPlan(List.of(covClass)));
    // More than one
    covClass.type(
        CodeableConcept.builder().coding(List.of(Coding.builder().code("group").build())).build());
    assertBadRequestBodyThrown(() -> _transformer().groupPlan(List.of(covClass, covClass)));
  }

  @Test
  void insuranceType() {
    // Null
    assertBadRequestBodyThrown(() -> _transformer().insuranceType(null));
    // Not a valid record coordinate id
    assertBadRequestBodyThrown(
        () ->
            _transformer()
                .insuranceType(
                    List.of(Reference.builder().reference("Organization/NOPE").build())));
    // More than one
    assertBadRequestBodyThrown(
        () ->
            _transformer()
                .insuranceType(List.of(Reference.builder().build(), Reference.builder().build())));
  }

  @Test
  void patientId() {
    // Null
    assertBadRequestBodyThrown(() -> _transformer().patientId(null));
    // Not MB
    assertBadRequestBodyThrown(
        () ->
            _transformer()
                .patientId(
                    Reference.builder()
                        .identifier(
                            Identifier.builder()
                                .type(
                                    CodeableConcept.builder()
                                        .coding(List.of(Coding.builder().code("NOT_MB").build()))
                                        .build())
                                .build())
                        .build()));
  }

  @Test
  void patientRelationshipHipaa() {
    // Null
    assertBadRequestBodyThrown(() -> _transformer().patientRelationshipHipaa(null));
    // Too Many Codes
    assertBadRequestBodyThrown(
        () ->
            _transformer()
                .patientRelationshipHipaa(
                    CodeableConcept.builder()
                        .coding(
                            List.of(
                                Coding.builder().code("spouse").build(),
                                Coding.builder().code("other").build()))
                        .build()));
    // Not a valid code
    assertBadRequestBodyThrown(
        () ->
            _transformer()
                .patientRelationshipHipaa(
                    CodeableConcept.builder()
                        .coding(List.of(Coding.builder().code("NOPE").build()))
                        .build()));
  }

  @Test
  void policyStartAndEndDates() {
    var period = Period.builder().start(Instant.parse("1992-01-12T12:34:56Z").toString()).build();
    assertThat(_transformer().policyStartAndEndDates(period))
        .containsOnly(
            WriteableFilemanValue.builder()
                .file(InsuranceType.FILE_NUMBER)
                .field(InsuranceType.EFFECTIVE_DATE_OF_POLICY)
                .index(1)
                .value("01-12-1992")
                .build());
    period.end(Instant.parse("2025-01-01T10:20:30Z").toString());
    assertThat(_transformer().policyStartAndEndDates(period))
        .containsExactlyInAnyOrder(
            WriteableFilemanValue.builder()
                .file(InsuranceType.FILE_NUMBER)
                .field(InsuranceType.EFFECTIVE_DATE_OF_POLICY)
                .index(1)
                .value("01-12-1992")
                .build(),
            WriteableFilemanValue.builder()
                .file(InsuranceType.FILE_NUMBER)
                .field(InsuranceType.INSURANCE_EXPIRATION_DATE)
                .index(1)
                .value("01-01-2025")
                .build());
  }

  @Test
  void policyStartAndEndDatesEndBeforeStartThrows() {
    var start = Instant.now();
    var period =
        Period.builder()
            .start(start.toString())
            .end(start.minus(1, ChronoUnit.HOURS).toString())
            .build();
    assertThatExceptionOfType(BadRequestPayload.class)
        .isThrownBy(() -> _transformer().policyStartAndEndDates(period));
  }

  @Test
  void requiredFields() {
    assertBadRequestBodyThrown(() -> _transformer().policyStartAndEndDates(null));
    assertBadRequestBodyThrown(
        () -> _transformer().policyStartAndEndDates(Period.builder().build()));
    assertBadRequestBodyThrown(() -> _transformer().stopPolicyFromBilling(null));
    assertBadRequestBodyThrown(
        () -> _transformer().stopPolicyFromBilling(Extension.builder().build()));
    assertBadRequestBodyThrown(() -> _transformer().subscriberId(null));
  }

  @Test
  void toInsuranceTypeFile() {
    var expected = CoverageSamples.VistaLhsLighthouseRpcGateway.create().updateApiInput();
    assertThat(_transformer().toInsuranceTypeFile()).containsExactlyInAnyOrderElementsOf(expected);
  }
}
