package gov.va.api.health.vistafhirquery.service.charonclient;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToCreateDuplicateRecord;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToSetInvalidFieldValue;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToSetUnknownField;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.AttemptToUpdateUnknownRecord;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.DoNotUnderstandRpcResponse;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.RejectedForMultipleReasons;
import gov.va.api.health.vistafhirquery.service.controller.LhsGatewayExceptions.UnknownReason;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LhsGatewayErrorHandlerTest {

  public static final String ANYTHING = "(?s).*";

  private static Stream<Arguments> lhsGatewayErrorsByCode() {
    return Stream.of(
        arguments("501", "x", AttemptToSetUnknownField.class),
        arguments("601", "x", AttemptToUpdateUnknownRecord.class),
        arguments("701", "generic message", AttemptToSetInvalidFieldValue.class),
        arguments(
            "701",
            "The value 'V' for field SOME FIELD in file SOME FILE is not valid whatever.",
            AttemptToSetInvalidFieldValue.class),
        arguments("999", "FOO already exists with IEN 123", AttemptToCreateDuplicateRecord.class),
        arguments("000", "x", UnknownReason.class));
  }

  private Map<String, String> errorDeets(String code, String text) {
    return Map.of("code", code, "location", "ignored", "text", text);
  }

  private Map<String, String> errorDeetsForCode(String code) {
    return errorDeets(code, "ignored");
  }

  @ParameterizedTest
  @MethodSource
  void lhsGatewayErrorsByCode(String errorCode, String text, Class<Throwable> expectedException) {
    assertThatExceptionOfType(expectedException)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeets(errorCode, text))
                                        .build()))
                            .results(List.of(resultsEntryFailure()))
                            .build())
                    .validateResults())
        .withMessageMatching(ANYTHING);
  }

  @Test
  void multipleErrorReasons() {
    assertThatExceptionOfType(RejectedForMultipleReasons.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeetsForCode("601"))
                                        .build(),
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeetsForCode("601"))
                                        .build()))
                            .results(List.of(resultsEntryFailure(), resultsEntryFailure()))
                            .build())
                    .validateResults())
        .withMessageMatching(ANYTHING);
  }

  @Test
  void noCodeFromVistaThrows() {
    assertThatExceptionOfType(UnknownReason.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(Map.of())
                                        .build()))
                            .build())
                    .validateResults())
        .withMessageMatching(ANYTHING);
  }

  @Test
  void recordDoesExistThrowsExceptionWhenMoreThanOneFailureIsFound() {
    assertThatExceptionOfType(DoNotUnderstandRpcResponse.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeetsForCode("601"))
                                        .build()))
                            .results(List.of(resultsEntryFailure(), resultsEntryFailure()))
                            .build())
                    .validateResults())
        .withMessageMatching(ANYTHING);
  }

  private FilemanEntry resultsEntryFailure() {
    return FilemanEntry.builder().file("ignored").ien("ignored").index("1").status("-1").build();
  }

  @Test
  void tooManyResultErrorsThrows() {
    assertThatExceptionOfType(DoNotUnderstandRpcResponse.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeetsForCode("601"))
                                        .build()))
                            .results(List.of(resultsEntryFailure(), resultsEntryFailure()))
                            .build())
                    .validateResults())
        .withMessageMatching(ANYTHING);
  }

  @Test
  void unknownReasonIsThrownIfEntriesHaveErrorsButNoErrorsAreAttached() {
    assertThatExceptionOfType(DoNotUnderstandRpcResponse.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .results(List.of(resultsEntryFailure()))
                            .build())
                    .validateResults())
        .withMessageMatching(ANYTHING);
  }
}
