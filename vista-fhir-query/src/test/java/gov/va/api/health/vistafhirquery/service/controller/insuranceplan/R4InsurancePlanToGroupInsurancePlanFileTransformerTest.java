package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class R4InsurancePlanToGroupInsurancePlanFileTransformerTest {
  static Stream<Arguments> missingExtensions() {
    return Stream.of(
        Arguments.of(List.of()),
        Arguments.of(
            List.of(
                Extension.builder()
                    .url(
                        "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired")
                    .valueBoolean(true)
                    .build())),
        Arguments.of(
            List.of(
                Extension.builder()
                    .url(
                        "http://va.gov/fhir/StructureDefinition/insuranceplan-isUtilizationReviewRequired")
                    .valueBoolean(true)
                    .build(),
                Extension.builder()
                    .url(
                        "http://va.gov/fhir/StructureDefinition/insuranceplan-isPreCertificationRequired")
                    .valueBoolean(true)
                    .build(),
                Extension.builder()
                    .url(
                        "http://va.gov/fhir/StructureDefinition/insuranceplan-excludePreexistingConditions")
                    .valueBoolean(false)
                    .build(),
                Extension.builder()
                    .url(
                        "http://va.gov/fhir/StructureDefinition/insuranceplan-areBenefitsAssignable")
                    .valueBoolean(true)
                    .build(),
                Extension.builder()
                    .url(
                        "http://va.gov/fhir/StructureDefinition/insuranceplan-isCertificationRequiredForAmbulatoryCare")
                    .valueBoolean(true)
                    .build())));
  }

  private R4InsurancePlanToGroupInsurancePlanFileTransformer _transformer() {
    return R4InsurancePlanToGroupInsurancePlanFileTransformer.builder()
        .insurancePlan(InsurancePlanSamples.R4.create().insurancePlan())
        .build();
  }

  void assertBadRequestBodyThrown(ThrowableAssert.ThrowingCallable r) {
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class).isThrownBy(r);
  }

  @Test
  void electronicPlanType() {
    // Null
    assertBadRequestBodyThrown(() -> _transformer().electronicPlanType(null));
    // Empty
    var codeableConcept =
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().system("WRONG_SYSTEM").build()))
            .build();
    assertBadRequestBodyThrown(() -> _transformer().electronicPlanType(List.of(codeableConcept)));
  }

  @Test
  void insuranceCompany() {
    // Null
    assertThat((_transformer().insuranceCompany(null))).isEqualTo(Optional.empty());
  }

  @ParameterizedTest
  @MethodSource
  void missingExtensions(List<Extension> extensions) {
    var blankExtensionTransformer =
        R4InsurancePlanToGroupInsurancePlanFileTransformer.builder()
            .insurancePlan(InsurancePlanSamples.R4.create().insurancePlan().extension(extensions))
            .build();
    assertBadRequestBodyThrown(blankExtensionTransformer::toGroupInsurancePlanFile);
  }

  @Test
  void planCategory() {
    var codeableConcept =
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().system("WRONG_SYSTEM").build()))
            .build();
    assertThat((_transformer().planCategory(List.of(codeableConcept)))).isEqualTo(Optional.empty());
  }

  @Test
  void planStandardFtf() {
    // Null extension
    assertBadRequestBodyThrown(() -> _transformer().planStandardFtf(null));
    // Null quantity
    assertBadRequestBodyThrown(() -> _transformer().planStandardFtf(Extension.builder().build()));
  }

  @Test
  void requiredFields() {
    assertBadRequestBodyThrown(
        () -> _transformer().extensionToBooleanWriteableFilemanValue(null, ".1234"));
  }

  @Test
  void toGroupInsurancePlanFile() {
    var expected = InsurancePlanSamples.VistaLhsLighthouseRpcGateway.create().createApiInput();
    assertThat(_transformer().toGroupInsurancePlanFile())
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void typeOfPlan() {
    // Null
    assertBadRequestBodyThrown(() -> _transformer().typeOfPlan(null));
    // Empty
    var planClass = InsurancePlan.Plan.builder().build();
    assertBadRequestBodyThrown(() -> _transformer().typeOfPlan(List.of(planClass)));
    // Null coding
    planClass.type(CodeableConcept.builder().build());
    assertBadRequestBodyThrown(() -> _transformer().typeOfPlan(List.of(planClass)));
    // Wrong system
    planClass.type(
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().system("WRONG_SYSTEM").build()))
            .build());
    assertBadRequestBodyThrown(() -> _transformer().typeOfPlan(List.of(planClass)));
    // More than one plan
    assertBadRequestBodyThrown(() -> _transformer().typeOfPlan(List.of(planClass, planClass)));
    // More than one coding
    planClass.type(
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().build(), Coding.builder().build()))
            .build());
    assertBadRequestBodyThrown(() -> _transformer().typeOfPlan(List.of(planClass)));
  }
}
