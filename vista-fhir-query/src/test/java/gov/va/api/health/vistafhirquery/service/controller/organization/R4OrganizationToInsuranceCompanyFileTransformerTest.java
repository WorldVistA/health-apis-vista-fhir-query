package gov.va.api.health.vistafhirquery.service.controller.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.resources.Organization;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredListItem;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.organization.R4OrganizationToInsuranceCompanyFileTransformer.ContactPurpose;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class R4OrganizationToInsuranceCompanyFileTransformerTest {
  private R4OrganizationToInsuranceCompanyFileTransformer _transformer() {
    return R4OrganizationToInsuranceCompanyFileTransformer.builder()
        .organization(OrganizationSamples.R4.create().organization())
        .include277EdiNumber(true)
        .build();
  }

  @Test
  void address() {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .address(
                        ".address",
                        ContactPurpose.BILL.name(),
                        "street1",
                        "street2",
                        "street3",
                        "city",
                        "state",
                        "zipcode",
                        Address.builder().build()));
    assertThat(
            _transformer()
                .address(
                    ".address",
                    ContactPurpose.BILL.name(),
                    "street1",
                    "street2",
                    "street3",
                    "city",
                    "state",
                    "zipcode",
                    null))
        .isEmpty();
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(
            () ->
                _transformer()
                    .address(
                        ".address",
                        ContactPurpose.BILL.name(),
                        "street1",
                        "street2",
                        "street3",
                        "city",
                        "state",
                        "zipcode",
                        Address.builder().line(List.of("", "", "")).build()));
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .address(
                        ".address",
                        ContactPurpose.BILL.name(),
                        "street1",
                        "street2",
                        "street3",
                        "city",
                        "state",
                        "zipcode",
                        Address.builder().line(List.of("", "", "", "")).build()));
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(
            () ->
                _transformer()
                    .address(
                        ".address",
                        ContactPurpose.BILL.name(),
                        "street1",
                        "street2",
                        "street3",
                        "city",
                        "state",
                        "zipcode",
                        Address.builder().line(List.of("1", "2", "3")).build()));
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(
            () ->
                _transformer()
                    .address(
                        ".address",
                        ContactPurpose.BILL.name(),
                        "street1",
                        "street2",
                        "street3",
                        "city",
                        "state",
                        "zipcode",
                        Address.builder()
                            .line(List.of("street1", "street2", "street3"))
                            .city("city")
                            .build()));
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(
            () ->
                _transformer()
                    .address(
                        ".address",
                        ContactPurpose.BILL.name(),
                        "street1",
                        "street2",
                        "street3",
                        "city",
                        "state",
                        "zipcode",
                        Address.builder()
                            .line(List.of("street1", "street2", "street3"))
                            .city("city")
                            .state("state")
                            .build()));
  }

  @Test
  void addressThrowsUnexpectedNumberOfValuesException() {
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(() -> _transformer().addressOrDie(Collections.emptyList()));
  }

  @Test
  void appealContactThrowMissingRequiredExceptions() {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                _transformer()
                    .contact(ContactPurpose.APPEAL, Organization.Contact.builder().build()));
  }

  @Test
  void contactPointForSystemThrowsRequestPayloadExceptions() {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().contactPurposeOrDie(null));
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().contactPurposeOrDie(CodeableConcept.builder().build()));
    assertThatExceptionOfType(UnexpectedNumberOfValues.class)
        .isThrownBy(
            () ->
                _transformer()
                    .contactPurposeOrDie(
                        CodeableConcept.builder()
                            .coding(List.of(Coding.builder().build(), Coding.builder().build()))
                            .build()));
  }

  @Test
  void contactPointThrowsMissingRequiredExceptions() {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(() -> _transformer().contactPoint(null, null, null, null));
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(
            () ->
                _transformer()
                    .contactPoint(
                        List.of(ContactPoint.builder().build()),
                        null,
                        null,
                        ContactPoint.ContactPointSystem.url));
  }

  @Test
  void empty() {
    assertThatExceptionOfType(MissingRequiredField.class)
        .isThrownBy(
            () ->
                R4OrganizationToInsuranceCompanyFileTransformer.builder()
                    .organization(Organization.builder().build())
                    .build()
                    .toInsuranceCompanyFile());
  }

  @Test
  void forSystemReturnEmptyOnNull() {
    _transformer().extensionForSystem(null, null).isEmpty();
  }

  @Test
  void identifiersThrowMissingRequiredExceptions() {
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(() -> _transformer().identifier(null, "", true));
  }

  @Test
  void missingIdentifiersThrowMissingRequiredExceptions() {
    var missingIdentifiersTransformer =
        R4OrganizationToInsuranceCompanyFileTransformer.builder()
            .organization(Organization.builder().build())
            .build();
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(() -> missingIdentifiersTransformer.identifier("", "", true));
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(() -> missingIdentifiersTransformer.n277EdiIdentifier());
  }

  @EnumSource(
      value = ContactPurpose.class,
      names = {"APPEAL", "DENTALCLAIMS", "RXCLAIMS"},
      mode = EnumSource.Mode.EXCLUDE)
  @ParameterizedTest
  void requiredContactsThrowMissingRequiredExceptions(ContactPurpose contactPurpose) {
    assertThatExceptionOfType(MissingRequiredListItem.class)
        .isThrownBy(
            () -> _transformer().contact(contactPurpose, Organization.Contact.builder().build()));
  }

  @Test
  void toInsuranceCompanyFile() {
    var expected = OrganizationSamples.VistaLhsLighthouseRpcGateway.create().createApiInput();
    assertThat(_transformer().toInsuranceCompanyFile())
        .containsExactlyInAnyOrderElementsOf(expected);
  }
}
