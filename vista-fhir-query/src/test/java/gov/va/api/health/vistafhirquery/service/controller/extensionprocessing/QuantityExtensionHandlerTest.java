package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.AbstractExtensionHandler.IsRequired;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

public class QuantityExtensionHandlerTest {
  static Stream<Arguments> badExtensionQuantity() {
    return Stream.of(
        arguments(Quantity.builder().build()),
        arguments(Quantity.builder().value(new BigDecimal("8.88")).build()),
        arguments(Quantity.builder().unit("SHANKS").build()));
  }

  private QuantityExtensionHandler _handler() {
    return QuantityExtensionHandler.builder()
        .definingUrl("http://fugazi.com/quantity")
        .required(IsRequired.REQUIRED)
        .valueFieldNumber("1")
        .unitFieldNumber("2")
        .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
        .build();
  }

  @ParameterizedTest
  @NullSource
  @MethodSource
  void badExtensionQuantity(Quantity quantity) {
    var sample = extensionWithQuantity(quantity);
    assertThatExceptionOfType(BadExtension.class).isThrownBy(() -> _handler().handle(sample));
  }

  private Extension extensionWithQuantity(Quantity quantity) {
    return Extension.builder().url("http://fugazi.com/quantity").valueQuantity(quantity).build();
  }

  @Test
  void handleQuantity() {
    var sample =
        extensionWithQuantity(
            Quantity.builder().value(new BigDecimal("7.88")).unit("SHANKS").build());
    assertThat(_handler().handle(sample))
        .containsExactlyInAnyOrder(
            WriteableFilemanValue.builder().file("888").index(1).field("1").value("7.88").build(),
            WriteableFilemanValue.builder()
                .file("888")
                .index(1)
                .field("2")
                .value("SHANKS")
                .build());
  }
}
