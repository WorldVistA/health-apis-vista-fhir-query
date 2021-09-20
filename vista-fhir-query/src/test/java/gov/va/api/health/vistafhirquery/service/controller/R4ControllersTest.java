package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.health.vistafhirquery.service.controller.R4Controllers.verifyAndGetResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.ExpectationFailed;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayResponse.Results;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class R4ControllersTest {
  @Test
  void verifyAndGetResultMoreThanOneResultThrowsExpectationFailed() {
    assertThatExceptionOfType(ExpectationFailed.class)
        .isThrownBy(() -> verifyAndGetResult(List.of("1", "2"), "publicId"));
  }

  @Test
  void verifyAndGetResultNoResultsThrowsNotFound() {
    assertThatExceptionOfType(ResourceExceptions.NotFound.class)
        .isThrownBy(() -> verifyAndGetResult(List.of(), "publicId"));
  }

  @Test
  void verifyAndGetResultOneResultReturnsResult() {
    assertThat(verifyAndGetResult(List.of("1"), "publicId")).isEqualTo("1");
  }

  @Test
  void verifySiteSpecificVistaResponseOrDie() {
    // Not exactly one result
    assertThatExceptionOfType(ExpectationFailed.class)
        .isThrownBy(
            () ->
                R4Controllers.verifySiteSpecificVistaResponseOrDie(
                    "123",
                    LhsLighthouseRpcGatewayResponse.builder().resultsByStation(Map.of()).build()));
    assertThatExceptionOfType(ExpectationFailed.class)
        .isThrownBy(
            () ->
                R4Controllers.verifySiteSpecificVistaResponseOrDie(
                    "123",
                    LhsLighthouseRpcGatewayResponse.builder()
                        .resultsByStation(
                            Map.of("1", Results.builder().build(), "2", Results.builder().build()))
                        .build()));
    // Exactly one result, but wrong site
    assertThatExceptionOfType(ExpectationFailed.class)
        .isThrownBy(
            () ->
                R4Controllers.verifySiteSpecificVistaResponseOrDie(
                    "123",
                    LhsLighthouseRpcGatewayResponse.builder()
                        .resultsByStation(Map.of("NOPE", Results.builder().build()))
                        .build()));
  }
}
