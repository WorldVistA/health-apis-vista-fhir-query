package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.InvalidNestedExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class ComplexExtensionHandlerTest {
  private ComplexExtensionHandler _handler() {
    return ComplexExtensionHandler.forDefiningUrl("http://fugazi.com/complex")
        .required(Required.REQUIRED)
        .childExtensions(
            List.of(
                StringExtensionHandler.forDefiningUrl("http://fugazi.com/string")
                    .required(Required.REQUIRED)
                    .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
                    .fieldNumber(".string")
                    .index(1)
                    .build()))
        .build();
  }

  private Extension complexExtensionFor(List<Extension> childExtensions) {
    return Extension.builder().url("http://fugazi.com/complex").extension(childExtensions).build();
  }

  @Test
  void handleComplexExtension() {
    var sample =
        complexExtensionFor(
            List.of(
                Extension.builder()
                    .url("http://fugazi.com/string")
                    .valueString("SHANKTOPUS")
                    .build()));
    assertThat(_handler().handle(".fugazi", sample))
        .containsOnly(
            WriteableFilemanValue.builder()
                .file("888")
                .index(1)
                .field(".string")
                .value("SHANKTOPUS")
                .build());
  }

  @Test
  void invalidNestedExceptionThrows() {
    var sample =
        complexExtensionFor(List.of(Extension.builder().url("http://fugazi.com/string").build()));
    assertThatExceptionOfType(InvalidNestedExtension.class)
        .isThrownBy(() -> _handler().handle(".fugazi", sample));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void nullOrEmptyExtensionsThrows(List<Extension> childExtensions) {
    var sample = complexExtensionFor(childExtensions);
    assertThatExceptionOfType(ExtensionMissingRequiredField.class)
        .isThrownBy(() -> _handler().handle(".fugazi", sample));
  }
}
