package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExactlyOneOfFields;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidConditionalField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidNestedExtension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForExtensionField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForField;
import java.util.List;
import org.junit.jupiter.api.Test;

class RequestPayloadExceptionsTest {
  @Test
  void atLeastOneOfExtensionFields() {
    assertThat(
            RequestPayloadExceptions.ExpectedAtLeastOneOfExtensionFields.builder()
                .jsonPath(".fugazi")
                .expectedAtLeastOneOfFields(List.of(".fugazi.foo", ".fugazi.bar"))
                .definingUrl("atLeastOneOf/url")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains(
            "Problem: Expected at least one of fields ([.fugazi.foo, .fugazi.bar]), but got none.")
        .contains("Extension: atLeastOneOf/url");
  }

  @Test
  void duplicateExtension() {
    assertThat(
            RequestPayloadExceptions.DuplicateExtension.builder()
                .jsonPath(".fugazi")
                .definingUrl("duped/url")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Defined extension is duplicated.")
        .contains("duped/url");
  }

  @Test
  void endDateBeforeStartDate() {
    assertThat(
            RequestPayloadExceptions.EndDateOccursBeforeStartDate.builder()
                .jsonPath(".fugaziDate")
                .build()
                .getPublicMessage())
        .contains("Field: .fugaziDate")
        .contains("Problem: End date occurs before the start date.");
  }

  @Test
  void exactlyOneOfFields() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                ExactlyOneOfFields.builder()
                    .jsonPath(".fugazi")
                    .exactlyOneOfFields(List.of())
                    .providedFields(List.of("a", "b"))
                    .build());
    assertThat(
            ExactlyOneOfFields.builder()
                .jsonPath(".fugazi")
                .providedFields(List.of(".fugazi.foo", ".fugazi.bar"))
                .exactlyOneOfFields(List.of(".fugazi.foo", ".fugazi.bar"))
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains(
            "Problem: Expected exactly one of ([.fugazi.foo, .fugazi.bar]), but none were provided.");
  }

  @Test
  void extensionHasInvalidReferenceId() {
    assertThat(
            RequestPayloadExceptions.ExtensionHasInvalidReferenceId.builder()
                .jsonPath(".fugazi")
                .definingUrl("invalidReferenceId/url")
                .referenceType("Resource")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Invalid Resource id.")
        .contains("Extension: invalidReferenceId/url");
  }

  @Test
  void extensionHasUnexpectedNumberOfValues() {
    assertThat(
            RequestPayloadExceptions.ExtensionFieldHasUnexpectedNumberOfValues.builder()
                .jsonPath(".fugazi")
                .receivedCount(2)
                .expectedCount(1)
                .identifyingFieldJsonPath(".fugazi.foo")
                .definingUrl("unexpectedNumber/url")
                .identifyingFieldValue(List.of("foo"))
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Unexpected number of values; expected (1), but got (2)")
        .contains("Extension: unexpectedNumber/url");
  }

  @Test
  void extensionMissingRequiredField() {
    assertThat(
            RequestPayloadExceptions.ExtensionMissingRequiredField.builder()
                .jsonPath(".fugazi")
                .definingUrl("missingReqField/url")
                .requiredFieldJsonPath(".fugazi.foo")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Missing required field: .fugazi.foo")
        .contains("missingReqField/url");
  }

  @Test
  void invalidConditionalField() {
    assertThat(
            InvalidConditionalField.builder()
                .jsonPath(".fugazi")
                .condition("Go baby go!")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem:")
        .contains("Condition: Go baby go!");
  }

  @Test
  void invalidNestedExtension() {
    assertThat(
            InvalidNestedExtension.builder()
                .jsonPath(".fugazi")
                .definingUrl("http://fugazi.parent")
                .nestedProblem("Ew, David!")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem:")
        .contains("Nested: (Ew, David!)");
  }

  @Test
  void invalidReferenceId() {
    assertThat(
            RequestPayloadExceptions.InvalidReferenceId.builder()
                .jsonPath(".fugazi")
                .referenceType("Resource")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Invalid Resource id.");
  }

  @Test
  void missingDefinitionUrl() {
    assertThat(
            RequestPayloadExceptions.MissingDefinitionUrl.builder()
                .jsonPath(".fugazi")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: An extension is missing it's .url field and cannot be processed.");
  }

  @Test
  void missingRequiredExtension() {
    assertThat(
            RequestPayloadExceptions.MissingRequiredExtension.builder()
                .jsonPath(".fugazi")
                .definingUrl("missingExtension")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Required extension is missing.")
        .contains("Extension: missingExtension");
  }

  @Test
  void missingRequiredField() {
    assertThat(
            RequestPayloadExceptions.MissingRequiredField.builder()
                .jsonPath(".fugazi")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Required field is missing.");
  }

  @Test
  void missingRequiredListItem() {
    assertThat(
            RequestPayloadExceptions.MissingRequiredListItem.builder()
                .jsonPath(".fugazi")
                .qualifiers(List.of("sadness", "more sadness"))
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Required item in list is missing.")
        .contains("Qualifiers: ([sadness, more sadness])");
  }

  @Test
  void unexpectedNumberOfValues() {
    assertThat(
            RequestPayloadExceptions.UnexpectedNumberOfValues.builder()
                .jsonPath(".fugazi")
                .receivedCount(2)
                .exactExpectedCount(1)
                .minimumExpectedCount(1)
                .maximumExpectedCount(1)
                .identifyingFieldJsonPath(".fugazi.foo")
                .identifyingFieldValue("foo")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains(
            "Problem: Unexpected number of values; expected (1) minimum of (1) maximum of (1) where .fugazi.foo matched foo. but got (2)");
  }

  @Test
  void unexpectedValueForExtensionField() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> UnexpectedValueForExtensionField.builder().build());
  }

  @Test
  void unexpectedValueForField() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> UnexpectedValueForField.builder().build());
    assertThat(
            RequestPayloadExceptions.UnexpectedValueForField.builder()
                .jsonPath(".fugazi")
                .dataType("foo")
                .valueSet("foo")
                .supportedValues(List.of("foo"))
                .valueReceived("bar")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Expected value to be of data type (foo), but got (bar).");
    assertThat(
            RequestPayloadExceptions.UnexpectedValueForField.builder()
                .jsonPath(".fugazi")
                .valueSet("foo")
                .valueReceived("bar")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Expected value from value-set (foo), but got (bar).");
    assertThat(
            RequestPayloadExceptions.UnexpectedValueForField.builder()
                .jsonPath(".fugazi")
                .supportedValues(List.of("foo"))
                .valueReceived("bar")
                .build()
                .getPublicMessage())
        .contains("Field: .fugazi")
        .contains("Problem: Expected one of ([foo]), but got (bar).");
  }
}
