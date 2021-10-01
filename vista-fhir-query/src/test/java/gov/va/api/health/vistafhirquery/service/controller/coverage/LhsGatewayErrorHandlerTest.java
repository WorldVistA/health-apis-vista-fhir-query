package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.vistafhirquery.service.charonclient.LhsGatewayErrorHandler;
import gov.va.api.health.vistafhirquery.service.charonclient.LhsGatewayErrorHandler.LhsGatewayError;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.CannotUpdateUnknownResource;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.MultipleErrorReasons;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.UnknownErrorReason;
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
  private static Stream<Arguments> lhsGatewayErrors() {
    return Stream.of(
        arguments(LhsGatewayError.INVALID_FIELD, BadRequestPayload.class),
        arguments(LhsGatewayError.RECORD_DOESNT_EXIST, CannotUpdateUnknownResource.class),
        arguments(LhsGatewayError.UNKNOWN, UnknownErrorReason.class));
  }

  private Map<String, String> errorDeets(String code) {
    return Map.of("code", code, "location", "ignored", "text", "ignored");
  }

  @ParameterizedTest
  @MethodSource("lhsGatewayErrors")
  void handleErrors(LhsGatewayError error, Class<Throwable> expectedException) {
    assertThatExceptionOfType(expectedException)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeets(error.code()))
                                        .build()))
                            .results(List.of(resultsEntryFailure()))
                            .build())
                    .validateResults());
  }

  @Test
  void multipleErrorReasons() {
    assertThatExceptionOfType(MultipleErrorReasons.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeets(LhsGatewayError.INVALID_FIELD.code()))
                                        .build(),
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(
                                            errorDeets(LhsGatewayError.RECORD_DOESNT_EXIST.code()))
                                        .build()))
                            .results(List.of(resultsEntryFailure(), resultsEntryFailure()))
                            .build())
                    .validateResults());
  }

  @Test
  void noCodeFromVistaThrows() {
    assertThatExceptionOfType(UnknownErrorReason.class)
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
                    .validateResults());
  }

  @Test
  void recordDoesExistThrowsExceptionWhenMoreThanOneFailureIsFound() {
    assertThatExceptionOfType(ExpectationFailed.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(
                                            errorDeets(LhsGatewayError.RECORD_DOESNT_EXIST.code()))
                                        .build()))
                            .results(List.of(resultsEntryFailure(), resultsEntryFailure()))
                            .build())
                    .validateResults());
  }

  private FilemanEntry resultsEntryFailure() {
    return FilemanEntry.builder().file("ignored").ien("ignored").index("1").status("-1").build();
  }

  @Test
  void tooManyResultErrorsThrows() {
    assertThatExceptionOfType(ExpectationFailed.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(
                                            errorDeets(LhsGatewayError.RECORD_DOESNT_EXIST.code()))
                                        .build()))
                            .results(List.of(resultsEntryFailure(), resultsEntryFailure()))
                            .build())
                    .validateResults());
  }

  @Test
  void unknownReasonIsThrownIfEntriesHaveErrorsButNoErrorsAreAttached() {
    assertThatExceptionOfType(UnknownErrorReason.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .results(List.of(resultsEntryFailure()))
                            .build())
                    .validateResults());
  }
}
