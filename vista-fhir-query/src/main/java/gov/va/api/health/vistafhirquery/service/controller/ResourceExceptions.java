package gov.va.api.health.vistafhirquery.service.controller;

import static java.lang.String.format;

import com.google.errorprone.annotations.FormatMethod;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/** The because methods exist to add readability when throwing exceptions. */
@SuppressWarnings("DoNotCallSuggester")
public class ResourceExceptions {

  /** BadSearchParameters . */
  public static final class BadSearchParameters extends ResourceException
      implements HasPublicMessage {
    public BadSearchParameters(String message) {
      super(message);
    }

    public static BadSearchParameters because(String message) {
      return new BadSearchParameters(message);
    }

    public String getPublicMessage() {
      return getMessage();
    }
  }

  public static final class CannotUpdateResourceWithMismatchedIds extends ResourceException {
    @Getter private final String idFromUrl;
    @Getter private final String idFromResource;

    /**
     * New instance specifying the ID as determined by the URL and the ID from resource, which may
     * be null.
     */
    public CannotUpdateResourceWithMismatchedIds(@NonNull String idFromUrl, String idFromResource) {
      super(format("id from URL: %s, id from resource: %s", idFromUrl, idFromResource));
      this.idFromUrl = idFromUrl;
      this.idFromResource = idFromResource;
    }

    public static CannotUpdateResourceWithMismatchedIds because(
        String idFromUrl, String idFromResource) {
      return new CannotUpdateResourceWithMismatchedIds(idFromUrl, idFromResource);
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

  public static final class MismatchedFileCoordinates extends ResourceException {
    public MismatchedFileCoordinates(String message) {
      super(message);
    }

    /** Prewritten error message for a file and resource. */
    public static MismatchedFileCoordinates because(
        String publicId, String[] supportedFileNumbers, String requestedFile) {
      return new MismatchedFileCoordinates(
          format(
              "%s contains mismatched site coordinates: Expected(%s) Received(%s)",
              publicId, Arrays.toString(supportedFileNumbers), requestedFile));
    }
  }

  /** The resource was not found. */
  public static final class NotFound extends ResourceException implements HasPublicMessage {
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

    @Override
    public String getPublicMessage() {
      return getMessage();
    }
  }

  /** Base exception for resource related errors. */
  public static class ResourceException extends RuntimeException {
    ResourceException(String message) {
      super(message);
    }
  }
}
