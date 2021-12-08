package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForExtensionField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BooleanExtensionHandlerTest {

  private final WriteableFilemanValueFactory filemanFactory =
      WriteableFilemanValueFactory.forFile("file number");

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void handle(int index) {
    assertThat(
            BooleanExtensionHandler.forDefiningUrl("defining url")
                .filemanFactory(filemanFactory)
                .fieldNumber("field number")
                .index(index)
                .booleanStringMapping(Map.of(true, "YES", false, "NO"))
                .required(ExtensionHandler.Required.REQUIRED)
                .build()
                .handle(
                    ".fugazi", Extension.builder().url("defining url").valueBoolean(true).build()))
        .containsOnly(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file("file number")
                .field("field number")
                .index(index)
                .value("YES")
                .build());
  }

  @Test
  void handleNullFromMappingIsNull() {
    assertThatExceptionOfType(UnexpectedValueForExtensionField.class)
        .isThrownBy(
            () ->
                BooleanExtensionHandler.forDefiningUrl("defining url")
                    .filemanFactory(filemanFactory)
                    .fieldNumber("field number")
                    .booleanStringMapping(Map.of(false, "NO"))
                    .required(ExtensionHandler.Required.REQUIRED)
                    .build()
                    .handle(
                        ".fugazi",
                        Extension.builder().url("defining url").valueBoolean(true).build()));
  }

  @Test
  void handleNullValueBooleanThrowsBadException() {
    assertThatExceptionOfType(ExtensionMissingRequiredField.class)
        .isThrownBy(
            () ->
                BooleanExtensionHandler.forDefiningUrl("defining url")
                    .filemanFactory(filemanFactory)
                    .fieldNumber("field number")
                    .booleanStringMapping(Map.of(true, "YES", false, ""))
                    .required(ExtensionHandler.Required.REQUIRED)
                    .build()
                    .handle(
                        ".fugazi",
                        Extension.builder().url("defining url").valueBoolean(null).build()));
  }
}
