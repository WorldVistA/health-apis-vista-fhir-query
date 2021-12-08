package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class IntegerExtensionHandlerTest {
  private IntegerExtensionHandler _handler(int index) {
    return IntegerExtensionHandler.forDefiningUrl("http://fugazi.com/integer")
        .required(Required.REQUIRED)
        .filemanFactory(WriteableFilemanValueFactory.forFile("integer"))
        .index(index)
        .fieldNumber("#.integer")
        .build();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void handle(int index) {
    assertThat(_handler(index).handle(".fugazi", integerExtension(8)))
        .containsOnly(
            WriteableFilemanValue.builder()
                .file("integer")
                .field("#.integer")
                .index(index)
                .value("8")
                .build());
  }

  @Test
  void handleNullThrows() {
    assertThatExceptionOfType(ExtensionMissingRequiredField.class)
        .isThrownBy(() -> _handler(1).handle(".fugazi", integerExtension(null)));
  }

  private Extension integerExtension(Integer value) {
    return Extension.builder().url("http://fugazi.com/integer").valueInteger(value).build();
  }
}
