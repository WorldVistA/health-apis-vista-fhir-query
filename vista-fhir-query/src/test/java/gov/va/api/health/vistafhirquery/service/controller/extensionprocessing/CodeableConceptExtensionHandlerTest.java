package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionFieldHasUnexpectedNumberOfValues;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class CodeableConceptExtensionHandlerTest {
  private static Stream<Arguments> badExtensionCodeableConcepts() {
    var goodCoding = Coding.builder().system("http://fugazi.com/coding").code("SHANKTOPUS").build();
    return Stream.of(
        arguments(ExtensionMissingRequiredField.class, null),
        arguments(
            ExtensionFieldHasUnexpectedNumberOfValues.class, CodeableConcept.builder().build()),
        arguments(
            ExtensionFieldHasUnexpectedNumberOfValues.class,
            CodeableConcept.builder().coding(List.of()).build()),
        arguments(
            ExtensionMissingRequiredField.class,
            CodeableConcept.builder().coding(goodCoding.code(null).asList()).build()),
        arguments(
            ExtensionFieldHasUnexpectedNumberOfValues.class,
            CodeableConcept.builder().coding(List.of(goodCoding, goodCoding)).build()));
  }

  private CodeableConceptExtensionHandler _handler(int index) {
    return CodeableConceptExtensionHandler.forDefiningUrl("http://fugazi.com/codeableConcept")
        .required(REQUIRED)
        .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
        .fieldNumber("#.88")
        .index(index)
        .codingSystem("http://fugazi.com/coding")
        .build();
  }

  @ParameterizedTest
  @MethodSource
  void badExtensionCodeableConcepts(Class<Exception> expectedException, CodeableConcept cc) {
    var sample = extensionWithCodeableConcept(cc);
    assertThatExceptionOfType(expectedException)
        .isThrownBy(() -> _handler(1).handle(".fugazi", sample));
  }

  private Extension extensionWithCodeableConcept(CodeableConcept codeableConcept) {
    return Extension.builder()
        .url("http://fugazi.com/codeableConept")
        .valueCodeableConcept(codeableConcept)
        .build();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void handle(int index) {
    var sample =
        extensionWithCodeableConcept(
            CodeableConcept.builder()
                .coding(
                    Coding.builder()
                        .system("http://fugazi.com/coding")
                        .code("SHANKTOPUS")
                        .build()
                        .asList())
                .build());
    assertThat(_handler(index).handle(".fugazi", sample))
        .containsOnly(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file("888")
                .field("#.88")
                .index(index)
                .value("SHANKTOPUS")
                .build());
  }
}
