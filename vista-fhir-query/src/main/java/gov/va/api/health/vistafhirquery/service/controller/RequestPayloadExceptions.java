package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static java.lang.String.format;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public class RequestPayloadExceptions {
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class BadRequestPayload extends RuntimeException implements HasPublicMessage {
    private BadRequestPayload(String message) {
      super(message);
    }

    @Override
    public String getPublicMessage() {
      return getMessage();
    }
  }

  public static class DuplicateExtension extends InvalidExtension {
    @Builder
    DuplicateExtension(String jsonPath, String definingUrl) {
      super(jsonPath, definingUrl, "Defined extension is duplicated.");
    }
  }

  public static class EndDateOccursBeforeStartDate extends InvalidField {
    @Builder
    EndDateOccursBeforeStartDate(String jsonPath) {
      super(jsonPath, "End date occurs before the start date.");
    }
  }

  @EqualsAndHashCode(callSuper = true)
  public static class InvalidConditionalField extends InvalidField {
    @NonNull @Getter private final String condition;

    @Builder
    InvalidConditionalField(String jsonPath, @NonNull String condition) {
      super(jsonPath, "Expected condition was not met.");
      this.condition = condition;
    }

    @Override
    public String getMessage() {
      return super.getMessage() + ", Condition: " + condition();
    }
  }

  @EqualsAndHashCode(callSuper = true)
  public static class InvalidExtension extends InvalidField {
    @NonNull @Getter private final String definingUrl;

    InvalidExtension(String jsonPath, String definingUrl, String problem) {
      super(jsonPath, problem);
      this.definingUrl = definingUrl;
    }

    @Override
    public String getMessage() {
      return super.getMessage() + ", Extension: " + definingUrl();
    }
  }

  @AllArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  public static class InvalidField extends BadRequestPayload {
    @NonNull @Getter private final String jsonPath;

    @NonNull @Getter private final String problem;

    @Override
    public String getMessage() {
      return format("Field: %s, Problem: %s", jsonPath(), problem());
    }
  }

  public static class InvalidReferenceId extends InvalidField {
    @Builder
    InvalidReferenceId(String jsonPath, String referenceType) {
      super(jsonPath, format("Invalid %s id.", referenceType));
    }
  }

  public static class MissingDefinitionUrl extends InvalidExtension {
    @Builder
    MissingDefinitionUrl(String jsonPath) {
      super(jsonPath, "", "An extension is missing it's .url field and cannot be processed.");
    }
  }

  public static class MissingRequiredExtension extends InvalidExtension {
    @Builder
    MissingRequiredExtension(String jsonPath, String definingUrl) {
      super(jsonPath, definingUrl, "Required extension is missing.");
    }
  }

  public static class MissingRequiredField extends InvalidField {
    @Builder
    MissingRequiredField(String jsonPath) {
      super(jsonPath, "Required field is missing.");
    }
  }

  public static class UnexpectedNumberOfValues extends InvalidField {
    UnexpectedNumberOfValues(String jsonPath, String problem) {
      super(jsonPath, problem);
    }

    @Builder
    private static UnexpectedNumberOfValues unexpectedNumberOfValues(
        @NonNull String jsonPath,
        String identifyingFieldJsonPath,
        String identifyingFieldValue,
        int expectedCount,
        int receivedCount) {
      var message =
          format(
              "Unexpected number of values; expected (%d), but got (%d)",
              expectedCount, receivedCount);
      if (!isBlank(identifyingFieldJsonPath)) {
        message += format(" where %s matched %s.", identifyingFieldJsonPath, identifyingFieldValue);
      }
      return new UnexpectedNumberOfValues(jsonPath, message);
    }
  }

  public static class UnexpectedValueForField extends InvalidField {
    UnexpectedValueForField(String jsonPath, String problem) {
      super(jsonPath, problem);
    }

    @Builder
    private static UnexpectedValueForField create(
        String jsonPath,
        String dataType,
        List<?> supportedValues,
        String valueSet,
        Object valueReceived) {
      if (!isBlank(dataType)) {
        return new UnexpectedValueForField(
            jsonPath,
            format(
                "Expected value to be of data type (%s), but got (%s).", dataType, valueReceived));
      }
      if (!isBlank(supportedValues)) {
        return new UnexpectedValueForField(
            jsonPath,
            format("Expected one of (%s), but got (%s).", supportedValues, valueReceived));
      }
      if (!isBlank(valueSet)) {
        return new UnexpectedValueForField(
            jsonPath,
            format("Expected value from value-set (%s), but got (%s).", valueSet, valueReceived));
      }
      throw new IllegalStateException(
          "One of dataType, supportedValues, or valueSet should be populated.");
    }
  }

  public static class ExactlyOneOfFields extends InvalidField {
    ExactlyOneOfFields(String jsonPath, String problem) {
      super(jsonPath, problem);
    }

    @Builder
    private static ExactlyOneOfFields exactlyOneOfFields(
        String jsonPath, @NonNull List<String> exactlyOneOfFields, List<String> providedFields) {
      if (exactlyOneOfFields.size() < 2) {
        throw new IllegalStateException("exactlyOneOfFields should have at least 2 entries.");
      }
      var message = format("Expected exactly one of (%s), ", exactlyOneOfFields);
      if (!isBlank(providedFields)) {
        message += "but none were provided.";
      } else {
        message += format("but (%s) were provided.", providedFields);
      }
      return new ExactlyOneOfFields(jsonPath, message);
    }
  }

  public static class UnknownExtension extends InvalidExtension {
    @Builder
    UnknownExtension(String jsonPath, String definingUrl) {
      super(jsonPath, definingUrl, "Defined extension is unknown.");
    }
  }
}
