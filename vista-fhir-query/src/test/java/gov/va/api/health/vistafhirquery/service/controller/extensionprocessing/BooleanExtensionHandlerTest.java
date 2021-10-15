package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class BooleanExtensionHandlerTest {

  private final WriteableFilemanValueFactory filemanFactory =
      WriteableFilemanValueFactory.forFile("file number");

  @Test
  void handle() {
    assertThat(
            BooleanExtensionHandler.forDefiningUrl("defining url")
                .filemanFactory(filemanFactory)
                .fieldNumber("field number")
                .booleanStringMapping(Map.of(true, "YES", false, "NO"))
                .required(AbstractExtensionHandler.IsRequired.REQUIRED)
                .build()
                .handle(Extension.builder().url("defining url").valueBoolean(true).build()))
        .containsOnly(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file("file number")
                .field("field number")
                .index(1)
                .value("YES")
                .build());
  }

  @Test
  void handleNullFromMappingIsNull() {
    assertThat(
            BooleanExtensionHandler.forDefiningUrl("defining url")
                .filemanFactory(filemanFactory)
                .fieldNumber("field number")
                .booleanStringMapping(Map.of(false, "NO"))
                .required(AbstractExtensionHandler.IsRequired.REQUIRED)
                .build()
                .handle(Extension.builder().url("defining url").valueBoolean(true).build()))
        .isEmpty();
  }

  @Test
  void handleNullValueBooleanThrowsBadException() {
    assertThatExceptionOfType(BadExtension.class)
        .isThrownBy(
            () ->
                BooleanExtensionHandler.forDefiningUrl("defining url")
                    .filemanFactory(filemanFactory)
                    .fieldNumber("field number")
                    .booleanStringMapping(Map.of(true, "YES", false, ""))
                    .required(AbstractExtensionHandler.IsRequired.REQUIRED)
                    .build()
                    .handle(Extension.builder().url("defining url").valueBoolean(null).build()));
  }
}
