package gov.va.api.health.vistafhirquery.service.controller.insuranceplan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredExtension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredListItem;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceCompany;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

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

  @Test
  void electronicPlanType() {
    // Null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy((() -> _transformer().electronicPlanType(null)));
    // Empty
    var codeableConcept =
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().system("WRONG_SYSTEM").build()))
            .build();
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(() -> _transformer().electronicPlanType(List.of(codeableConcept)));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void groupName(String name) {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().groupName(name));
  }

  @Test
  void insuranceCompany() {
    // Null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().ownedBy(null));
    // Invalid Reference
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer().ownedBy(Reference.builder().reference("Organization/NOPE").build()));
    // Valid
    assertThat(_transformer().ownedBy(Reference.builder().reference("Organization/x;y;z").build()))
        .usingRecursiveComparison()
        .ignoringFields("index")
        .isEqualTo(
            WriteableFilemanValue.builder()
                .file(InsuranceCompany.FILE_NUMBER)
                .field("ien")
                .value("z")
                .index(0)
                .build());
  }

  @ParameterizedTest
  @MethodSource
  void missingExtensions(List<Extension> extensions) {
    var blankExtensionTransformer =
        R4InsurancePlanToGroupInsurancePlanFileTransformer.builder()
            .insurancePlan(InsurancePlanSamples.R4.create().insurancePlan().extension(extensions))
            .build();
    assertThatExceptionOfType(MissingRequiredExtension.class)
        .isThrownBy(blankExtensionTransformer::toGroupInsurancePlanFile);
  }

  @Test
  void ownedBy() {
    // Null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().ownedBy(null));
    // Invalid Reference
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer().ownedBy(Reference.builder().reference("Organization/NOPE").build()));
    // Valid
    assertThat(_transformer().ownedBy(Reference.builder().reference("Organization/x;y;z").build()))
        .usingRecursiveComparison()
        .ignoringFields("index")
        .isEqualTo(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file(InsuranceCompany.FILE_NUMBER)
                .field("ien")
                .value("z")
                .index(0)
                .build());
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
  void toGroupInsurancePlanFile() {
    var expected = InsurancePlanSamples.VistaLhsLighthouseRpcGateway.create().createApiInput();
    assertThat(_transformer().toGroupInsurancePlanFile())
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test
  void typeOfPlan() {
    // Null
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().typeOfPlan(null));
    // Empty
    var planClass = InsurancePlan.Plan.builder().build();
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().typeOfPlan(List.of(planClass)));
    // Null coding
    planClass.type(CodeableConcept.builder().build());
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().typeOfPlan(List.of(planClass)));
    // Wrong system
    planClass.type(
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().system("WRONG_SYSTEM").build()))
            .build());
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(() -> _transformer().typeOfPlan(List.of(planClass)));
    // More than one plan
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(() -> _transformer().typeOfPlan(List.of(planClass, planClass)));
    // More than one coding
    planClass.type(
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().build(), Coding.builder().build()))
            .build());
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(() -> _transformer().typeOfPlan(List.of(planClass)));
  }
}
