package gov.va.api.health.vistafhirquery.service.charonclient;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToCreateDuplicateRecord;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToReferenceInvalidRecord;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToSetInvalidFieldValue;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToSetUnknownField;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToUpdateRecordWithMismatchedDfn;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToUpdateUnknownRecord;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToWriteRecordMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.DoNotUnderstandRpcResponse;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.LhsGatewayException;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.RecordNotFound;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.RejectedForMultipleReasons;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.UnknownReason;
import gov.va.api.health.vistafhirquery.service.controller.R4Controllers.FatalServerError;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.ResultsError;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor(staticName = "of")
public class LhsGatewayErrorHandler {
  private static final List<ErrorDetector> ERROR_DETECTORS =
      List.of(
          ErrorDetection.detectInvalidField(),
          ErrorDetection.detectInvalidIen(),
          ErrorDetection.detectMissingRequiredField(),
          ErrorDetection.detectUnknownField(),
          ErrorDetection.detectUpdateRecordWithMismatchedDfn(),
          ErrorDetection.detectRecordDoesNotExist(),
          ErrorDetection.detectRecordAlreadyExists(),
          ErrorDetection.detectRecordNotFound());

  private final LhsLighthouseRpcGatewayResponse.Results results;

  /** If there are any errors that can be collected from the response, throw a fatal error. */
  public static void dieOnReadError(LhsLighthouseRpcGatewayResponse response) {
    var errors = response.collectErrors();
    if (errors.isEmpty()) {
      return;
    }
    throw new FatalServerError(response.toString());
  }

  private boolean hasFailedResult() {
    return results.results().stream().anyMatch(FilemanEntries::isFailure);
  }

  private LhsGatewayException toException(ResultsError error) {
    return ERROR_DETECTORS.stream()
        .map(detector -> detector.apply(results, error))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseGet(() -> UnknownReason.builder().errorData(error.data()).build());
  }

  /** Verify there are errors and deal with them appropriately. */
  public void validateResults() {
    if (hasFailedResult() && !results.hasError()) {
      throw DoNotUnderstandRpcResponse.builder()
          .publicMessage("Unexpected remote procedure call response.")
          .privateMessage("Result indicate failures but there are no attached errors.")
          .build();
    }
    if (!results.hasError()) {
      return;
    }
    List<LhsGatewayException> exceptions =
        results.errors().stream().map(this::toException).collect(toList());
    if (exceptions.size() == 1) {
      throw exceptions.get(0);
    }
    throw RejectedForMultipleReasons.builder().reasons(exceptions).build();
  }

  private static class ErrorDetection {
    /**
     * Detect this error pattern.
     *
     * <pre>
     *   The value 'GRP123' for field GROUP NUMBER in file GROUP INSURANCE PLAN is not valid.
     * </pre>
     */
    static ErrorDetector detectInvalidField() {
      var pattern =
          Pattern.compile(
              "The value '([^']+)'.* for field ([A-Z0-9 ]+).* in file ([A-Z0-9 ]+) is not valid.*");
      return ErrorDetector.builder()
          .ifCondition(isCode("701"))
          .thenThrow(
              (r, e) -> {
                var matcher = pattern.matcher(textOf(e));
                String publicMessage;
                if (matcher.matches()) {
                  var field = matcher.group(2);
                  var file = matcher.group(3);
                  publicMessage =
                      format(
                          "Invalid %s for %s.",
                          field.toLowerCase(Locale.US).trim(), file.toLowerCase(Locale.US).trim());
                } else {
                  publicMessage =
                      "Attempt to set field was rejected for an unknown reason. Contact support.";
                }
                return AttemptToSetInvalidFieldValue.builder()
                    .publicMessage(publicMessage)
                    .errorData(e.data())
                    .build();
              })
          .build();
    }

    /**
     * Detect this error pattern.
     *
     * <pre>
     *   code=1,
     *   location=Create Patient Insurance Type,
     *   text=Group Insurance Plan for IEN not valid for Index Record (1).
     * </pre>
     */
    static ErrorDetector detectInvalidIen() {
      var pattern =
          Pattern.compile(
              "([A-Za-z ]+) (for IEN not valid|ICN not in database) for Index Record .*");
      return ErrorDetector.builder()
          .ifCondition(isTextPattern(pattern))
          .thenThrow(
              (r, e) -> {
                var matcher = pattern.matcher(textOf(e));
                if (matcher.matches()) {
                  return AttemptToReferenceInvalidRecord.builder()
                      .fileName(matcher.group(1))
                      .errorData(e.data())
                      .build();
                }
                return AttemptToReferenceInvalidRecord.builder()
                    .fileName("Unknown")
                    .errorData(e.data())
                    .build();
              })
          .build();
    }

    /**
     * Detect this error pattern.
     *
     * <pre>
     *   code=1
     *   location=Create IIV Response
     *   text=Message Control ID required for Index Record (1).
     * </pre>
     */
    static ErrorDetector detectMissingRequiredField() {
      var locationPattern = Pattern.compile("^(Create|Update) ([A-Za-z ]+)");
      var textPattern = Pattern.compile("(.*) required for Index Record .*");
      return ErrorDetector.builder()
          .ifCondition(isTextPattern(textPattern))
          .thenThrow(
              (r, e) -> {
                var fileNameMatcher = locationPattern.matcher(locationOf(e));
                var fieldNameMatcher = textPattern.matcher(textOf(e));
                return AttemptToWriteRecordMissingRequiredField.builder()
                    .fileName(fileNameMatcher.matches() ? fileNameMatcher.group(2) : "Unknown")
                    .fieldName(fieldNameMatcher.matches() ? fieldNameMatcher.group(1) : "Unknown")
                    .errorData(e.data())
                    .build();
              })
          .build();
    }

    /**
     * Detect this error pattern.
     *
     * <pre>
     *   text=Group Plan already exists with IEN (88) for Index Record (1)
     *   location=Create Group Insurance Plan
     * </pre>
     */
    static ErrorDetector detectRecordAlreadyExists() {
      return ErrorDetector.builder()
          .ifCondition(isTextPattern("^.* already exists with IEN .*"))
          .thenThrow(
              (r, e) ->
                  AttemptToCreateDuplicateRecord.builder()
                      .recordType(
                          e.data()
                              .getOrDefault("location", "unknown")
                              .replace("Create ", "")
                              .trim())
                      .errorData(e.data())
                      .build())
          .build();
    }

    /**
     * Detect this error pattern (UPDATE).
     *
     * <pre>
     *   code=601
     *   location=FILE^LHSIBUTL
     *   text=The entry does not exist.
     * </pre>
     */
    static ErrorDetector detectRecordDoesNotExist() {
      return ErrorDetector.builder()
          .ifCondition(isCode("601").and(isLocationPattern("FILE\\^LHSIBUTL")))
          .thenThrow(
              (r, e) -> {
                var failedEntries = failedFilemanEntriesOf(r);
                if (failedEntries.size() != 1) {
                  return multipleFailures(failedEntries);
                }
                var entry = failedEntries.get(0);
                return AttemptToUpdateUnknownRecord.builder()
                    .file(entry.file())
                    .ien(entry.ien())
                    .errorData(e.data())
                    .build();
              })
          .build();
    }

    /**
     * Detect this error pattern (READ).
     *
     * <pre>
     *   code=601
     *   location=$$GETS^LHSFM1
     *   text=The entry does not exist.
     * </pre>
     */
    static ErrorDetector detectRecordNotFound() {
      return ErrorDetector.builder()
          .ifCondition(isCode("601").and(isLocationPattern("\\$\\$GETS\\^LHSFM1")))
          .thenThrow((r, e) -> RecordNotFound.builder().errorData(e.data()).build())
          .build();
    }

    static ErrorDetector detectUnknownField() {
      return ErrorDetector.builder()
          .ifCondition(isCode("501"))
          .thenThrow((r, e) -> AttemptToSetUnknownField.builder().errorData(e.data()).build())
          .build();
    }

    /**
     * Detect this error pattern.
     *
     * <pre>
     *   code=1
     *   location=Update Patient Insurance Type
     *   text=Patient Insurance with IEN (1,999,) for Index Record (1)
     *     does not exist for patient DFN (706).
     * </pre>
     */
    static ErrorDetector detectUpdateRecordWithMismatchedDfn() {
      return ErrorDetector.builder()
          .ifCondition(
              isTextPattern(
                  "([A-Za-z ]+) with IEN \\(([0-9,]+)\\) for Index Record "
                      + "\\([0-9]+\\) does not exist for .*"))
          .thenThrow(
              (r, e) ->
                  AttemptToUpdateRecordWithMismatchedDfn.builder().errorData(e.data()).build())
          .build();
    }

    static List<FilemanEntry> failedFilemanEntriesOf(Results r) {
      return r.results().stream().filter(FilemanEntries::isFailure).toList();
    }

    static String fromErrorDetails(String fieldName, ResultsError error) {
      return error
          .data()
          .getOrDefault(
              fieldName, "unknown reason ('" + fieldName + "' is missing from error details)");
    }

    static Predicate<ResultsError> isCode(String code) {
      return e -> code.equals(e.data().get("code"));
    }

    static Predicate<ResultsError> isLocationPattern(String regex) {
      var pattern = Pattern.compile(regex);
      return isLocationPattern(pattern);
    }

    static Predicate<ResultsError> isLocationPattern(Pattern pattern) {
      return e -> pattern.matcher(e.data().getOrDefault("location", "")).matches();
    }

    static Predicate<ResultsError> isTextPattern(String regex) {
      var pattern = Pattern.compile(regex);
      return isTextPattern(pattern);
    }

    static Predicate<ResultsError> isTextPattern(Pattern pattern) {
      return e -> pattern.matcher(e.data().getOrDefault("text", "")).matches();
    }

    static String locationOf(ResultsError error) {
      return fromErrorDetails("location", error);
    }

    private static DoNotUnderstandRpcResponse multipleFailures(List<FilemanEntry> failedEntries) {
      return DoNotUnderstandRpcResponse.builder()
          .publicMessage("Multiple failed entries found.")
          .privateMessage(
              format(
                  "1 result expected to indicate a record does not exist," + " but found %d: %s",
                  failedEntries.size(),
                  failedEntries.stream().map(ErrorDetection::summarize).collect(joining("\n"))))
          .build();
    }

    private static String summarize(FilemanEntry entry) {
      return format("File %s, IEN %s, Status %s", entry.file(), entry.ien(), entry.status());
    }

    static String textOf(ResultsError error) {
      return fromErrorDetails("text", error);
    }
  }

  @Builder
  private static class ErrorDetector
      implements BiFunction<
          LhsLighthouseRpcGatewayResponse.Results, ResultsError, LhsGatewayException> {
    Predicate<ResultsError> ifCondition;

    BiFunction<LhsLighthouseRpcGatewayResponse.Results, ResultsError, LhsGatewayException>
        thenThrow;

    @Override
    public LhsGatewayException apply(Results results, ResultsError error) {
      if (ifCondition.test(error)) {
        return thenThrow.apply(results, error);
      }
      return null;
    }
  }
}
