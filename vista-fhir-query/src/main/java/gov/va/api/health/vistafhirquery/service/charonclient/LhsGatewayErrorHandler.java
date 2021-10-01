package gov.va.api.health.vistafhirquery.service.charonclient;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.CannotUpdateUnknownResource;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.MultipleErrorReasons;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ResourceException;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.UnknownErrorReason;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.ResultsError;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(staticName = "of")
public class LhsGatewayErrorHandler {
  private static final String SUCCESS = "1";

  private final LhsLighthouseRpcGatewayResponse.Results results;

  private BadRequestPayload badRequestPayloadForInvalidField(ResultsError result) {
    return BadRequestPayload.because(
        "Vista Description: "
            + result
                .data()
                .getOrDefault("text", "unknown reason ('text' is missing from error details)"));
  }

  private List<String> describeFailedFilemanEntries() {
    return results.results().stream()
        .filter(this::isFailure)
        .map(
            failure ->
                format(
                    "File %s, IEN %s, Status %s", failure.file(), failure.ien(), failure.status()))
        .toList();
  }

  private boolean hasFailedResult() {
    return results.results().stream().anyMatch(this::isFailure);
  }

  private boolean isFailure(FilemanEntry entry) {
    return !isSuccessful(entry);
  }

  private boolean isSuccessful(FilemanEntry entry) {
    return SUCCESS.equals(entry.status());
  }

  @SuppressWarnings("EnhancedSwitchMigration")
  private ResourceException toException(ResultsError result) {
    var errorCode = result.data().get("code");
    if (isBlank(errorCode)) {
      return new UnknownErrorReason("Reason not available. 'code' missing from error details.");
    }
    LhsGatewayError error = LhsGatewayError.forCode(errorCode);
    switch (error) {
      case RECORD_DOESNT_EXIST:
        return unknownRecordVistaCoordinates();
      case INVALID_FIELD:
        return badRequestPayloadForInvalidField(result);
      default:
        return new UnknownErrorReason(result.data().toString());
    }
  }

  private ResourceException unknownRecordVistaCoordinates() {
    var failedEntries = describeFailedFilemanEntries();
    if (failedEntries.size() != 1) {
      return ExpectationFailed.because(
          format(
              "For %s errors, only 1 result is expected, but found %d: %s",
              LhsGatewayError.RECORD_DOESNT_EXIST,
              failedEntries.size(),
              join("\n", failedEntries)));
    }
    return new CannotUpdateUnknownResource(failedEntries.get(0));
  }

  /** Verify there are errors and deal with them appropriately. */
  public void validateResults() {
    if (hasFailedResult() && !results.hasError()) {
      throw new UnknownErrorReason("Result indicate failures but there are no attached errors");
    }

    if (!results.hasError()) {
      return;
    }
    List<ResourceException> exceptions =
        results.errors().stream().map(this::toException).collect(toList());
    if (exceptions.size() == 1) {
      throw exceptions.get(0);
    }
    throw new MultipleErrorReasons(exceptions);
  }

  @AllArgsConstructor
  public enum LhsGatewayError {
    RECORD_DOESNT_EXIST("601"),
    INVALID_FIELD("701"),
    UNKNOWN("000");

    @Getter private final String code;

    /** Determine an error type based on the error code. */
    public static LhsGatewayError forCode(@NonNull String code) {
      return Arrays.stream(LhsGatewayError.values())
          .filter(c -> code.equals(c.code()))
          .findFirst()
          .orElse(UNKNOWN);
    }
  }
}
