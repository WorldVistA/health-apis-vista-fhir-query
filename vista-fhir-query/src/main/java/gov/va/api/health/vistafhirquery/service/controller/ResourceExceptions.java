package gov.va.api.health.vistafhirquery.service.controller;

import static java.lang.String.format;

import com.google.errorprone.annotations.FormatMethod;

/** The because methods exist to add readability when throwing exceptions. */
@SuppressWarnings("DoNotCallSuggester")
public class ResourceExceptions {

  public static final class BadPayload extends ResourceException {
    public BadPayload(String message) {
      super(message);
    }

    public static BadPayload because(String message) {
      return new BadPayload(message);
    }

    public static BadPayload because(String vistaField, String reason) {
      return because(format("Could not populate vista field %s: %s", vistaField, reason));
    }
  }

  /** BadSearchParameters . */
  public static final class BadSearchParameters extends ResourceException {
    public BadSearchParameters(String message) {
      super(message);
    }

    public static BadSearchParameters because(String message) {
      return new BadSearchParameters(message);
    }
  }

  /** ExpectationFailed . */
  public static final class ExpectationFailed extends ResourceException {
    public ExpectationFailed(String message) {
      super(message);
    }

    public static ExpectationFailed because(String message) {
      return new ExpectationFailed(message);
    }

    @FormatMethod
    public static ExpectationFailed because(String message, Object... values) {
      return because(format(message, values));
    }
  }

  /** NotFound . */
  public static final class NotFound extends ResourceException {
    public NotFound(String message) {
      super(message);
    }

    public static NotFound because(String message) {
      return new NotFound(message);
    }

    @FormatMethod
    public static NotFound because(String message, Object... values) {
      return because(format(message, values));
    }
  }

  /** ResourceException . */
  static class ResourceException extends RuntimeException {
    ResourceException(String message) {
      super(message);
    }
  }
}
