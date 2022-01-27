package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.vistafhirquery.service.controller.IsSiteCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionHasInvalidReferenceId;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ReferenceExtensionHandlerTest {
  static Stream<Arguments> badExtensionReference() {
    return Stream.of(
        arguments(ExtensionMissingRequiredField.class, null),
        arguments(ExtensionHasInvalidReferenceId.class, Reference.builder().build()),
        arguments(
            ExtensionHasInvalidReferenceId.class,
            Reference.builder().display("SHANKTOPUS").build()));
  }

  private ReferenceExtensionHandler _handler(int index) {
    return ReferenceExtensionHandler.forDefiningUrl("http://fugazi.com/reference")
        .required(REQUIRED)
        .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
        .fieldNumber("#.88")
        .index(index)
        .referenceType("Fugazi")
        .toCoordinates(s -> new FugaziSiteCoordinates())
        .build();
  }

  @ParameterizedTest
  @MethodSource
  void badExtensionReference(Class<Exception> expected, Reference reference) {
    var sample = extensionWithReference(reference);
    assertThatExceptionOfType(expected).isThrownBy(() -> _handler(1).handle(".fugazi", sample));
  }

  private Extension extensionWithReference(Reference reference) {
    return Extension.builder().url("http://fugazi.com/reference").valueReference(reference).build();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void handle(int index) {
    var sample = extensionWithReference(Reference.builder().reference("Fugazi/123").build());
    assertThat(_handler(index).handle(".fugazi", sample))
        .containsOnly(
            WriteableFilemanValue.builder()
                .file("888")
                .index(index)
                .field("#.88")
                .value("`ien8")
                .build());
  }

  @Test
  void handlerThrowsWhenCoordinatesAreNull() {
    var sample = extensionWithReference(Reference.builder().reference("Fugazi/123").build());
    assertThatExceptionOfType(ExtensionHasInvalidReferenceId.class)
        .isThrownBy(
            () ->
                ReferenceExtensionHandler.forDefiningUrl("http://fugazi.com/reference")
                    .required(REQUIRED)
                    .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
                    .fieldNumber("#.88")
                    .referenceType("Fugazi")
                    .toCoordinates(s -> null)
                    .build()
                    .handle(".fugazi", sample));
  }

  public static class FugaziSiteCoordinates implements IsSiteCoordinates {
    @Override
    public String file() {
      return "f1";
    }

    @Override
    public String ien() {
      return "ien8";
    }

    @Override
    public String site() {
      return "s1";
    }
  }
}
