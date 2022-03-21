package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static gov.va.api.health.vistafhirquery.service.controller.coverage.CoverageStructureDefinitions.SUBSCRIBER_RELATIONSHIP_CODE_SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.InsurancePlan;
import gov.va.api.health.r4.api.resources.InsurancePlan.Plan;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.FilemanFactoryRegistry;
import gov.va.api.health.vistafhirquery.service.controller.FilemanIndexRegistry;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.EndDateOccursBeforeStartDate;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidStringLengthInclusively;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import gov.va.api.health.vistafhirquery.service.controller.coverage.R4CoverageToInsuranceBufferTransformer.ContainedInsurancePlanTransformer;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.InsuranceVerificationProcessor;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class R4CoverageToInsuranceBufferTransformerTest {
  private static Stream<Arguments> effectiveAndExpirationDateValidation() {
    return Stream.of(
        arguments(null, MissingRequiredField.class),
        arguments(Period.builder().build(), UnexpectedValueForField.class),
        arguments(Period.builder().start("badDate").build(), UnexpectedValueForField.class),
        arguments(
            Period.builder().start("1993-01-12T00:00:00Z").end("badDate").build(),
            UnexpectedValueForField.class),
        arguments(
            Period.builder().start("2025-01-01T00:00:00Z").end("1993-01-12T00:00:00Z").build(),
            EndDateOccursBeforeStartDate.class));
  }

  private static Stream<Arguments> typeOfPlanInvalidValueThrows() {
    var definition = InsuranceBufferDefinitions.get().typeOfPlan();
    return Stream.of(
        arguments(
            List.of(Plan.builder().build(), Plan.builder().build()),
            UnexpectedNumberOfValues.class),
        arguments(
            Plan.builder()
                .type(
                    CodeableConcept.builder()
                        .coding(
                            List.of(
                                Coding.builder().system(definition.valueSet()).display("X").build(),
                                Coding.builder()
                                    .system(definition.valueSet())
                                    .display("Y")
                                    .build()))
                        .build())
                .build()
                .asList(),
            UnexpectedNumberOfValues.class));
  }

  private static Stream<Arguments> typeOfPlanIsOptional() {
    return Stream.of(
        arguments(Plan.builder().build().asList()),
        arguments(Plan.builder().type(CodeableConcept.builder().build()).build().asList()));
  }

  private ContainedInsurancePlanTransformer _insurancePlan() {
    return ContainedInsurancePlanTransformer.builder()
        .insurancePlan(
            CoverageSamples.R4
                .create()
                .coverageInsuranceBufferRead("p1", "123", "c1")
                .contained()
                .stream()
                .filter(r -> InsurancePlan.class.getSimpleName().equals(r.resourceType()))
                .findFirst()
                .map(r -> (InsurancePlan) r)
                .get())
        .indexRegistry(FilemanIndexRegistry.create())
        .factoryRegistry(FilemanFactoryRegistry.create())
        .build();
  }

  private R4CoverageToInsuranceBufferTransformer _transformer() {
    return R4CoverageToInsuranceBufferTransformer.builder()
        .coverage(CoverageSamples.R4.create().coverageInsuranceBufferRead("p1", "123", "c1"))
        .build();
  }

  @Test
  void address() {
    assertThat(
            _transformer()
                .address(
                    InsuranceVerificationProcessor.CITY,
                    InsuranceVerificationProcessor.STATE,
                    InsuranceVerificationProcessor.ZIP_CODE,
                    addressLineFields(),
                    null))
        .isEmpty();
    assertThat(
            _transformer()
                .address(
                    InsuranceVerificationProcessor.CITY,
                    InsuranceVerificationProcessor.STATE,
                    InsuranceVerificationProcessor.ZIP_CODE,
                    addressLineFields(),
                    List.of(Address.builder().build())))
        .isEmpty();
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .address(
                        InsuranceVerificationProcessor.CITY,
                        InsuranceVerificationProcessor.STATE,
                        InsuranceVerificationProcessor.ZIP_CODE,
                        addressLineFields(),
                        List.of(Address.builder().build(), Address.builder().build())));
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .address(
                        InsuranceVerificationProcessor.CITY,
                        InsuranceVerificationProcessor.STATE,
                        InsuranceVerificationProcessor.ZIP_CODE,
                        addressLineFields(),
                        List.of(
                            Address.builder()
                                .line(
                                    List.of(
                                        "STREET ADDRESS LINE 1",
                                        "STREET ADDRESS LINE 2",
                                        "STREET ADDRESS LINE 3",
                                        "STREET ADDRESS LINE 4"))
                                .build())));
    assertThat(
            _transformer()
                .address(
                    InsuranceVerificationProcessor.CITY,
                    InsuranceVerificationProcessor.STATE,
                    InsuranceVerificationProcessor.ZIP_CODE,
                    addressLineFields(),
                    List.of(Address.builder().line(List.of("PO BOX 1798")).build())))
        .containsExactly(
            WriteableFilemanValue.builder()
                .file(InsuranceVerificationProcessor.FILE_NUMBER)
                .index(1)
                .field(InsuranceVerificationProcessor.STREET_ADDRESS_LINE_1)
                .value("PO BOX 1798")
                .build());
  }

  private List<String> addressLineFields() {
    return List.of(
        InsuranceVerificationProcessor.STREET_ADDRESS_LINE_1,
        InsuranceVerificationProcessor.STREET_ADDRESS_LINE_2,
        InsuranceVerificationProcessor.STREET_ADDRESS_LINE_3);
  }

  @Test
  void contactFor() {
    // Null list
    assertThat(_transformer().contactFor(null, "code", "field")).isNull();
    // Null field
    assertThat(
            _transformer()
                .contactFor(Organization.Contact.builder().build().asList(), "code", null))
        .isNull();
    // Null code
    assertThat(
            _transformer()
                .contactFor(Organization.Contact.builder().build().asList(), null, "field"))
        .isNull();
    // Null purpose
    assertThat(
            _transformer()
                .contactFor(
                    Organization.Contact.builder()
                        .telecom(
                            ContactPoint.builder()
                                .system(ContactPoint.ContactPointSystem.phone)
                                .value("800-CONTACT")
                                .build()
                                .asList())
                        .build()
                        .asList(),
                    "code",
                    "field"))
        .isNull();
    // Null coding
    assertThat(
            _transformer()
                .contactFor(
                    Organization.Contact.builder()
                        .telecom(
                            ContactPoint.builder()
                                .system(ContactPoint.ContactPointSystem.phone)
                                .value("800-CONTACT")
                                .build()
                                .asList())
                        .purpose(CodeableConcept.builder().build())
                        .build()
                        .asList(),
                    "code",
                    "field"))
        .isNull();
    // Null telecom
    assertThat(
            _transformer()
                .contactFor(
                    Organization.Contact.builder()
                        .purpose(
                            CodeableConcept.builder()
                                .coding(Coding.builder().code("code").build().asList())
                                .build())
                        .build()
                        .asList(),
                    "code",
                    "field"))
        .isNull();
    // Null system
    assertThat(
            _transformer()
                .contactFor(
                    Organization.Contact.builder()
                        .telecom(ContactPoint.builder().value("800-CONTACT").build().asList())
                        .purpose(
                            CodeableConcept.builder()
                                .coding(Coding.builder().code("code").build().asList())
                                .build())
                        .build()
                        .asList(),
                    "code",
                    "field"))
        .isNull();
    // Null value
    assertThat(
            _transformer()
                .contactFor(
                    Organization.Contact.builder()
                        .telecom(
                            ContactPoint.builder()
                                .system(ContactPoint.ContactPointSystem.phone)
                                .build()
                                .asList())
                        .purpose(
                            CodeableConcept.builder()
                                .coding(Coding.builder().code("code").build().asList())
                                .build())
                        .build()
                        .asList(),
                    "code",
                    "field"))
        .isNull();
    // Code not found
    assertThat(
            _transformer()
                .contactFor(
                    Organization.Contact.builder()
                        .telecom(
                            ContactPoint.builder()
                                .system(ContactPoint.ContactPointSystem.phone)
                                .value("800-CONTACT")
                                .build()
                                .asList())
                        .purpose(
                            CodeableConcept.builder()
                                .coding(Coding.builder().code("code").build().asList())
                                .build())
                        .build()
                        .asList(),
                    "not-found",
                    "field"))
        .isNull();
    // More than one
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .contactFor(
                        Organization.Contact.builder()
                            .telecom(
                                List.of(
                                    ContactPoint.builder()
                                        .system(ContactPoint.ContactPointSystem.phone)
                                        .value("800-CONTACT")
                                        .build(),
                                    ContactPoint.builder()
                                        .system(ContactPoint.ContactPointSystem.phone)
                                        .value("800-PHONE")
                                        .build()))
                            .purpose(
                                CodeableConcept.builder()
                                    .coding(Coding.builder().code("code").build().asList())
                                    .build())
                            .build()
                            .asList(),
                        "code",
                        "field"));
  }

  @Test
  void countryAndSubdivision() {
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .relatedPersonAddress(
                        List.of(Address.builder().build(), Address.builder().build())));
  }

  @ParameterizedTest
  @MethodSource
  void effectiveAndExpirationDateValidation(Period sample, Class<Throwable> isThrown) {
    assertThatExceptionOfType(isThrown)
        .isThrownBy(() -> _transformer().effectiveAndExpirationDate(sample));
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
  void expirationDateIsOptional() {
    assertThat(
            _transformer()
                .effectiveAndExpirationDate(Period.builder().start("1993-01-12T00:00:00Z").build()))
        .hasSize(1)
        .singleElement()
        .hasFieldOrPropertyWithValue("value", "2930112");
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
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().inqServiceTypeCode1(null));
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
  void nameOfInsured() {
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .nameOfInsured(
                        List.of(HumanName.builder().build(), HumanName.builder().build())));
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().nameOfInsured(null));
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().nameOfInsured(List.of()));
  }

  @Test
  void organizationStatus() {
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(() -> _transformer().organizationStatus(null));
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(() -> _transformer().organizationStatus(false));
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
                                            .coding(
                                                List.of(
                                                    Coding.builder()
                                                        .code("MB")
                                                        .system(
                                                            "http://terminology.hl7.org/CodeSystem/v2-0203")
                                                        .build()))
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
  void phoneNumber() {
    // Null list
    assertThat(_transformer().phoneNumber(null, "phoneField")).isNull();
    // Null system
    assertThat(
            _transformer()
                .phoneNumber(
                    List.of(ContactPoint.builder().value("800-PHONE").build()), "phoneField"))
        .isNull();
    // Null value
    assertThat(
            _transformer()
                .phoneNumber(
                    List.of(
                        ContactPoint.builder()
                            .system(ContactPoint.ContactPointSystem.phone)
                            .build()),
                    "phoneField"))
        .isNull();
    // More than one
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .phoneNumber(
                        List.of(
                            ContactPoint.builder()
                                .system(ContactPoint.ContactPointSystem.phone)
                                .value("800-PHONE")
                                .build(),
                            ContactPoint.builder()
                                .system(ContactPoint.ContactPointSystem.phone)
                                .value("800-PHONE")
                                .build()),
                        "phoneField"));
  }

  @Test
  void relatedPersonBirthDate() {
    assertThatExceptionOfType(UnexpectedValueForField.class)
        .isThrownBy(() -> _transformer().relatedPersonBirthDate("bad date"));
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().relatedPersonBirthDate(null));
  }

  @Test
  void serviceDate() {
    assertThat(_transformer().coverageExtensions(null)).isNotNull();
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
    assertThat(_transformer().toInsuranceBuffer()).containsExactlyInAnyOrderElementsOf(expected);
  }

  @ParameterizedTest
  @MethodSource
  void typeOfPlanInvalidValueThrows(List<Plan> sample, Class<Throwable> isThrown) {
    assertThatExceptionOfType(isThrown).isThrownBy(() -> _insurancePlan().typeOfPlan(sample));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @MethodSource
  void typeOfPlanIsOptional(List<Plan> sample) {
    assertThat(_insurancePlan().typeOfPlan(sample)).isEmpty();
  }
}
