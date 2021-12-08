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
  void exactlyOneOfFields() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                ExactlyOneOfFields.builder()
                    .jsonPath(".fugazi")
                    .exactlyOneOfFields(List.of())
                    .providedFields(List.of("a", "b"))
                    .build());
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
  void unexpectedValueForExtensionField() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> UnexpectedValueForExtensionField.builder().build());
  }

  @Test
  void unexpectedValueForField() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> UnexpectedValueForField.builder().build());
  }
}
