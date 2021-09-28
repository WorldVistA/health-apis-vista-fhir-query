package gov.va.api.health.vistafhirquery.service.controller.coverage;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.vistafhirquery.service.controller.R4Controllers.FatalServerError;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.CannotUpdateUnknownResource;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.health.vistafhirquery.service.controller.coverage.LhsGatewayErrorHandler.LhsGatewayError;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.FilemanEntry;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.UnexpectedVistaValue;
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
        arguments(LhsGatewayError.UNKNOWN, FatalServerError.class));
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
  void noCodeFromVistaThrows() {
    assertThatExceptionOfType(UnexpectedVistaValue.class)
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

  private FilemanEntry resultsEntryFailure() {
    return FilemanEntry.builder().file("ignored").ien("ignored").index("1").status("-1").build();
  }

  @Test
  void tooManyErrorsThrows() {
    assertThatExceptionOfType(ExpectationFailed.class)
        .isThrownBy(
            () ->
                LhsGatewayErrorHandler.of(
                        LhsLighthouseRpcGatewayResponse.Results.builder()
                            .errors(
                                List.of(
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeets("701"))
                                        .build(),
                                    LhsLighthouseRpcGatewayResponse.ResultsError.builder()
                                        .data(errorDeets("601"))
                                        .build()))
                            .build())
                    .validateResults());
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
}
