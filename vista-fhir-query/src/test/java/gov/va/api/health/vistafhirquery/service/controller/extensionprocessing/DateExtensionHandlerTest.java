package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableDateDefinition;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableExtensionDefinition;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DateExtensionHandlerTest {
  private DateExtensionHandler _handler(int index) {
    return DateExtensionHandler.builder()
        .definition(
            MappableExtensionDefinition.forValueDefinition(
                    MappableDateDefinition.builder()
                        .vistaField("#.field_number")
                        .isRequired(true)
                        .dateFormatter(DateTimeFormatter.ofPattern("MMddyyyy"))
                        .build())
                .structureDefinition("http://fugazi.com/date")
                .build())
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
                        .url("http://fugazi.com/date")
                        .valueDate("3333-01-02")
                        .build()))
        .containsOnly(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file("file_number")
                .index(index)
                .field("#.field_number")
                .value("01023333")
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
                        Extension.builder().url("http://fugazi.com/date").valueDate(null).build()));
  }
}
