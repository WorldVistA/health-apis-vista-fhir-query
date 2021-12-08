package gov.va.api.health.vistafhirquery.service.controller;

import static java.lang.String.format;
import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class LhsGatewayExceptions {

  public static class AttemptToCreateDuplicateRecord extends LhsGatewayRejectedRequest {
    @Builder
    private AttemptToCreateDuplicateRecord(String recordType, Map<String, String> errorData) {
      super("Request is attempting to create a duplicate " + recordType + " record.", errorData);
    }
  }

  public static class AttemptToSetInvalidFieldValue extends LhsGatewayRejectedRequest {

    @Builder
    private AttemptToSetInvalidFieldValue(
        @NonNull String publicMessage, @NonNull Map<String, String> errorData) {
      super(publicMessage, errorData);
    }
  }

  public static class AttemptToSetUnknownField extends LhsGatewayRejectedRequest {

    @Builder
    private AttemptToSetUnknownField(@NonNull Map<String, String> errorData) {
      super("Attempt to update unknown field. Contact support.", errorData);
    }
  }

  public static class AttemptToUpdateUnknownRecord extends LhsGatewayRejectedRequest {

    private final String file;

    private final String ien;

    @Builder
    private AttemptToUpdateUnknownRecord(
        String file, String ien, @NonNull Map<String, String> errorData) {
      super("Attempt to update unknown record. Contact support.", errorData);
      this.file = file;
      this.ien = ien;
    }

    @Override
    public String getMessage() {
      return format("File %s, IEN %s: ", file, ien) + super.getMessage();
    }
  }

  public static class DoNotUnderstandRpcResponse extends LhsGatewayException {

    private final String publicMessage;

    @Builder
    private DoNotUnderstandRpcResponse(String privateMessage, String publicMessage) {
      super(publicMessage + ": " + privateMessage);
      this.publicMessage = publicMessage;
    }

    @Override
    public String getPublicMessage() {
      return publicMessage;
    }
  }

  public abstract static class LhsGatewayException extends RuntimeException
      implements HasPublicMessage {

    LhsGatewayException() {}

    LhsGatewayException(String message) {
      super(message);
    }
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class LhsGatewayRejectedRequest extends LhsGatewayException
      implements HasPublicMessage {
    @NonNull private final String publicMessage;
    @Getter @NonNull private final Map<String, String> errorData;

    @Override
    public String getMessage() {
      return getPublicMessage()
          + " Details "
          + errorData().entrySet().stream()
              .sorted(comparingByKey())
              .map(e -> e.getKey() + "=" + e.getValue())
              .collect(joining(", "));
    }

    @Override
    public @NonNull String getPublicMessage() {
      return publicMessage;
    }
  }

  public static class RejectedForMultipleReasons extends LhsGatewayException {
    @Getter private final List<? extends LhsGatewayException> reasons;

    @Builder
    private RejectedForMultipleReasons(@NonNull List<? extends LhsGatewayException> reasons) {
      super(
          reasons.size()
              + " reasons:\n"
              + reasons.stream()
                  .map(e -> format("%s: %s", e.getClass().getSimpleName(), e.getMessage()))
                  .collect(joining("\n")));
      this.reasons = reasons;
    }

    @Override
    public String getPublicMessage() {
      return "Rejected for "
          + reasons().size()
          + " reasons:\n"
          + reasons().stream().map(HasPublicMessage::getPublicMessage).collect(joining("\n"));
    }
  }

  public static class UnknownReason extends LhsGatewayRejectedRequest {
    @Builder
    private UnknownReason(@NonNull Map<String, String> errorData) {
      super("Unknown reason", errorData);
    }
  }
}
