package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.vistafhirquery.service.controller.R4Controllers.FatalServerError;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.CannotUpdateUnknownResource;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.ResultsError;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.UnexpectedVistaValue;
import java.util.Arrays;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(staticName = "of")
public class LhsGatewayErrorHandler {
  private static final String SUCCESS = "1";

  private final LhsLighthouseRpcGatewayResponse.Results results;

  private void handleError(Map<String, String> errorDetails) {
    var errorCode = errorDetails.get("code");
    if (isBlank(errorCode)) {
      throw new UnexpectedVistaValue("code", null, "Code must be populated to handle RPC errors.");
    }
    LhsGatewayError error = LhsGatewayError.forCode(errorCode);
    switch (error) {
      case RECORD_DOESNT_EXIST -> throw CannotUpdateUnknownResource.because(
          unknownRecordVistaCoordinates());
      case INVALID_FIELD -> throw BadRequestPayload.because(
          "Vista Description: " + errorDetails.get("text"));
      case UNKNOWN -> throw new FatalServerError(errorDetails.toString());
      default -> throw new IllegalStateException(
          "Failed to determine appropriate error: " + errorDetails);
    }
  }

  private String unknownRecordVistaCoordinates() {
    var resultsWithFailures =
        results.results().stream().filter(r -> !SUCCESS.equals(r.status())).toList();
    if (resultsWithFailures.size() != 1) {
      throw ExpectationFailed.because(
          "Unexpected number of results are marked as failure: " + resultsWithFailures);
    }
    var failure = resultsWithFailures.get(0);
    return format("File(%s) Ien(%s)", failure.file(), failure.ien());
  }

  /** Verify there are errors and deal with them appropriately. */
  void validateResults() {
    if (!results.hasError()) {
      return;
    }
    if (results.errors().size() > 1) {
      throw new ExpectationFailed("Ambiguous error codes: " + results.errors());
    }
    results.errors().stream().map(ResultsError::data).forEach(this::handleError);
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
