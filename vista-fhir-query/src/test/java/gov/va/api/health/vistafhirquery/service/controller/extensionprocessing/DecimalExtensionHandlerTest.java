package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DecimalExtensionHandlerTest {
  private DecimalExtensionHandler _handler(int index) {
    return DecimalExtensionHandler.forDefiningUrl("http://fugazi.com/decimal")
        .required(Required.REQUIRED)
        .filemanFactory(WriteableFilemanValueFactory.forFile("decimal"))
        .index(index)
        .fieldNumber("#.decimal")
        .build();
  }

  private Extension decimalExtension(String maybeDecimal) {
    var value = maybeDecimal == null ? null : new BigDecimal(maybeDecimal);
    return Extension.builder().url("http://fugazi.com/decimal").valueDecimal(value).build();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void handle(int index) {
    assertThat(_handler(index).handle(".fugazi", decimalExtension("8.88000")))
        .containsOnly(
            WriteableFilemanValue.builder()
                .file("decimal")
                .field("#.decimal")
                .index(index)
                .value("8.88000")
                .build());
  }

  @Test
  void handleNullThrows() {
    assertThatExceptionOfType(ExtensionMissingRequiredField.class)
        .isThrownBy(() -> _handler(1).handle(".fugazi", decimalExtension(null)));
  }
}
