package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class QuantityExtensionHandlerTest {
  static Stream<Arguments> badExtensionQuantity() {
    return Stream.of(
        arguments(ExtensionMissingRequiredField.class, null),
        arguments(ExtensionMissingRequiredField.class, Quantity.builder().build()),
        arguments(
            ExtensionMissingRequiredField.class,
            Quantity.builder().value(new BigDecimal("8.88")).build()),
        arguments(ExtensionMissingRequiredField.class, Quantity.builder().unit("SHANKS").build()));
  }

  private QuantityExtensionHandler _handler(int index) {
    return QuantityExtensionHandler.forDefiningUrl("http://fugazi.com/quantity")
        .required(REQUIRED)
        .valueFieldNumber(".value")
        .unitFieldNumber(".unit")
        .index(index)
        .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
        .build();
  }

  @ParameterizedTest
  @MethodSource
  void badExtensionQuantity(Class<Exception> expected, Quantity quantity) {
    var sample = extensionWithQuantity(quantity);
    assertThatExceptionOfType(expected).isThrownBy(() -> _handler(1).handle(".fugazi", sample));
  }

  private Extension extensionWithQuantity(Quantity quantity) {
    return Extension.builder().url("http://fugazi.com/quantity").valueQuantity(quantity).build();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void handle(int index) {
    var sample =
        extensionWithQuantity(
            Quantity.builder().value(new BigDecimal("7.88")).unit("SHANKS").build());
    assertThat(_handler(index).handle(".fugazi", sample))
        .containsExactlyInAnyOrder(
            WriteableFilemanValue.builder()
                .file("888")
                .index(index)
                .field(".value")
                .value("7.88")
                .build(),
            WriteableFilemanValue.builder()
                .file("888")
                .index(index)
                .field(".unit")
                .value("SHANKS")
                .build());
  }

  @Test
  void handlerDoesNotRequireUnitField() {
    var handler =
        QuantityExtensionHandler.forDefiningUrl("http://fugazi.com/quantity")
            .required(REQUIRED)
            .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
            .valueFieldNumber(".value")
            .index(1)
            .build();
    var sample = extensionWithQuantity(Quantity.builder().value(new BigDecimal("8.88")).build());
    assertThat(handler.handle(".fugazi", sample))
        .containsOnly(
            WriteableFilemanValue.builder()
                .file("888")
                .index(1)
                .field(".value")
                .value("8.88")
                .build());
  }
}
