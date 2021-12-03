package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static java.lang.String.format;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
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

  public static class InvalidDateRange extends BadRequestPayload {
    InvalidDateRange(String message) {
      super(message);
    }

    @Builder
    private static InvalidDateRange invalidDateRange(@NonNull String jsonPath, String message) {
      return new InvalidDateRange(jsonPath + " has an invalid date range: " + message);
    }
  }

  public static class InvalidReferenceId extends BadRequestPayload {
    InvalidReferenceId(String message) {
      super(message);
    }

    @Builder
    private static InvalidReferenceId invalidReferenceId(
        @NonNull String jsonPath, @NonNull String referenceType) {
      return new InvalidReferenceId("Invalid " + referenceType + " id for field " + jsonPath);
    }
  }

  public static class MissingRequiredField extends BadRequestPayload {
    MissingRequiredField(String message) {
      super(message);
    }

    public static MissingRequiredField forJsonPath(String jsonPath) {
      return new MissingRequiredField("Required field " + jsonPath + " is missing.");
    }
  }

  public static class UnexpectedNumberOfValues extends BadRequestPayload {
    UnexpectedNumberOfValues(String message) {
      super(message);
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
              "Unexpected number of values for field %s: expected (%d), but got (%d)",
              jsonPath, expectedCount, receivedCount);
      if (!isBlank(identifyingFieldJsonPath)) {
        message += format(" where %s matched %s.", identifyingFieldJsonPath, identifyingFieldValue);
      }
      return new UnexpectedNumberOfValues(message);
    }
  }

  public static class UnexpectedValueForField extends BadRequestPayload {
    UnexpectedValueForField(String message) {
      super(message);
    }

    @Builder
    private static UnexpectedValueForField unexpectedValueForField(
        @NonNull String jsonPath, List<?> supportedValues, String valueSet, Object valueReceived) {
      if (isBlank(supportedValues) && isBlank(valueSet)) {
        throw new IllegalStateException("One of supportedValues or valueSet should be populated.");
      }
      var message = "Unexpected value for field " + jsonPath + ": ";
      if (!isBlank(valueSet)) {
        message +=
            format("Expected value from value-set (%s), but got (%s).", valueSet, valueReceived);
      } else {
        message += format("Expected one of (%s), but got (%s).", supportedValues, valueReceived);
      }
      return new UnexpectedValueForField(message);
    }
  }
}
