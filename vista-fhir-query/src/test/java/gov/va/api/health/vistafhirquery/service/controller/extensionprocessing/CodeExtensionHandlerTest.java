package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CodeExtensionHandlerTest {
  private CodeExtensionHandler _handler(int index) {
    return CodeExtensionHandler.forDefiningUrl("http://fugazi.com/code")
        .required(REQUIRED)
        .fieldNumber("#.field_number")
        .index(index)
        .filemanFactory(WriteableFilemanValueFactory.forFile("file_number"))
        .build();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void handle(int index) {
    assertThat(
            _handler(index)
                .handle(
                    ".fugazi",
                    Extension.builder()
                        .url("http://fugazi.com/code")
                        .valueCode("test_code")
                        .build()))
        .containsOnly(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file("file_number")
                .index(index)
                .field("#.field_number")
                .value("test_code")
                .build());
  }

  @Test
  void handleNullValueCodeThrowsBadException() {
    assertThatExceptionOfType(ExtensionMissingRequiredField.class)
        .isThrownBy(
            () ->
                _handler(1)
                    .handle(
                        ".fugazi",
                        Extension.builder().url("http://fugazi.com/code").valueCode(null).build()));
  }
}