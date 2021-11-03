package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class StringExtensionHandlerTest {
  private StringExtensionHandler _handler(int index) {
    return StringExtensionHandler.forDefiningUrl("http://fugazi.com/string")
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
                    Extension.builder()
                        .url("http://fugazi.com/string")
                        .valueString("test_string")
                        .build()))
        .containsOnly(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file("file_number")
                .index(index)
                .field("#.field_number")
                .value("test_string")
                .build());
  }

  @Test
  void handleNullValueStringThrowsBadException() {
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.BadExtension.class)
        .isThrownBy(
            () ->
                _handler(1)
                    .handle(
                        Extension.builder()
                            .url("http://fugazi.com/string")
                            .valueString(null)
                            .build()));
  }
}
