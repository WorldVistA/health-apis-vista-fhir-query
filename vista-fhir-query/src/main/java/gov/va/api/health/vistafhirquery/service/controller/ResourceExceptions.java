package gov.va.api.health.vistafhirquery.service.controller;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import com.google.errorprone.annotations.FormatMethod;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.lang.Nullable;

/** The because methods exist to add readability when throwing exceptions. */
@SuppressWarnings("DoNotCallSuggester")
public class ResourceExceptions {

  public static final class BadRequestPayload extends ResourceException {
    public BadRequestPayload(String message) {
      super(message);
    }

    public static BadRequestPayload because(String message) {
      return new BadRequestPayload(message);
    }

    public static BadRequestPayload because(String vistaField, String reason) {
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

  public static final class CannotUpdateResourceWithMismatchedIds extends ResourceException {
    @Getter private final String idFromUrl;
    @Getter private final String idFromResource;

    /**
     * New instance specifying the ID as determined by the URL and the ID from resource, which may
     * be null.
     */
    public CannotUpdateResourceWithMismatchedIds(
        @NonNull String idFromUrl, @Nullable String idFromResource) {
      super(format("id from URL: %s, id from resource: %s", idFromUrl, idFromResource));
      this.idFromUrl = idFromUrl;
      this.idFromResource = idFromResource;
    }

    public static CannotUpdateResourceWithMismatchedIds because(
        String idFromUrl, String idFromResource) {
      return new CannotUpdateResourceWithMismatchedIds(idFromUrl, idFromResource);
    }
  }

  public static final class CannotUpdateUnknownResource extends ResourceException {
    @Getter private final String resourceId;

    public CannotUpdateUnknownResource(String resourceId) {
      super(resourceId);
      this.resourceId = resourceId;
    }

    public static CannotUpdateUnknownResource because(String resourceId) {
      return new CannotUpdateUnknownResource(resourceId);
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

  /** More than one reason has caused a failure. */
  public static final class MultipleErrorReasons extends ResourceException {

    @Getter private final List<ResourceException> reasons;

    /** Look at those chickens. */
    public MultipleErrorReasons(List<ResourceException> reasons) {
      super(
          reasons.size()
              + " reasons:\n"
              + reasons.stream()
                  .map(e -> format("%s: %s", e.getClass().getSimpleName(), e.getMessage()))
                  .collect(joining("\n")));
      this.reasons = reasons;
    }
  }

  /** The resource was not found. */
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

  /** Base exception for resource related errors. */
  public static class ResourceException extends RuntimeException {
    ResourceException(String message) {
      super(message);
    }
  }

  /** Cannot determine cause of error. */
  public static final class UnknownErrorReason extends ResourceException {
    public UnknownErrorReason(String message) {
      super(message);
    }
  }
}
