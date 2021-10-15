package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.AbstractExtensionHandler.IsRequired;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

public class CodeableConceptExtensionHandlerTest {
  private static Stream<Arguments> badExtensionCodeableConcepts() {
    var goodCoding = Coding.builder().system("http://fugazi.com/coding").code("SHANKTOPUS").build();
    return Stream.of(
        arguments(CodeableConcept.builder().build()),
        arguments(CodeableConcept.builder().coding(List.of()).build()),
        arguments(CodeableConcept.builder().coding(goodCoding.code(null).asList()).build()),
        arguments(CodeableConcept.builder().coding(List.of(goodCoding, goodCoding)).build()));
  }

  private CodeableConceptExtensionHandler _handler() {
    return CodeableConceptExtensionHandler.forDefiningUrl("http://fugazi.com/codeableConcept")
        .required(IsRequired.REQUIRED)
        .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
        .fieldNumber("#.88")
        .codingSystem("http://fugazi.com/coding")
        .build();
  }

  @ParameterizedTest
  @NullSource
  @MethodSource
  void badExtensionCodeableConcepts(CodeableConcept cc) {
    var sample = extensionWithCodeableConcept(cc);
    assertThatExceptionOfType(BadExtension.class).isThrownBy(() -> _handler().handle(sample));
  }

  private Extension extensionWithCodeableConcept(CodeableConcept codeableConcept) {
    return Extension.builder()
        .url("http://fugazi.com/codeableConept")
        .valueCodeableConcept(codeableConcept)
        .build();
  }

  @Test
  void handleCodeableConcept() {
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
    assertThat(_handler().handle(sample))
        .containsOnly(
            LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue.builder()
                .file("888")
                .field("#.88")
                .index(1)
                .value("SHANKTOPUS")
                .build());
  }
}
