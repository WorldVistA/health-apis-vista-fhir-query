package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ExtensionsHandlersTest {
  @Test
  void extractCodeableConceptValueForSystem() {
    var cc =
        CodeableConcept.builder()
            .coding(
                List.of(
                    Coding.builder().system("FUGAZI SYSTEM").code("FUGAZI").build(),
                    Coding.builder().system("NACHO SYSTEM").code("NACHO").build()))
            .build();
    assertThat(ExtensionsHandlers.extractCodeableConceptValueForSystem(cc, "foo", "FUGAZI SYSTEM"))
        .isEqualTo("FUGAZI");
  }

  @Test
  void extractCodeableConceptValueForSystemThrowsBadPayloadWhenCodeableConceptCodingCodeIsNull() {
    var cc =
        CodeableConcept.builder()
            .coding(List.of(Coding.builder().system("FUGAZI").build()))
            .build();
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(
            () -> ExtensionsHandlers.extractCodeableConceptValueForSystem(cc, "foo", "FUGAZI"));
  }

  @Test
  void extractCodeableConceptValueForSystemThrowsBadPayloadWhenCodeableConceptCodingIsNull() {
    var cc = CodeableConcept.builder().build();
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(
            () -> ExtensionsHandlers.extractCodeableConceptValueForSystem(cc, "foo", "FUGAZI"));
  }

  @Test
  void extractCodeableConceptValueForSystemThrowsBadPayloadWhenCodeableConceptIsNull() {
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(
            () -> ExtensionsHandlers.extractCodeableConceptValueForSystem(null, null, null));
  }

  @Test
  void extractCodeableConceptValueForSystemThrowsBadRequestPayloadWhenDuplicateSystemFound() {
    var cc =
        CodeableConcept.builder()
            .coding(
                List.of(
                    Coding.builder().system("FUGAZI SYSTEM").code("FUGAZI").build(),
                    Coding.builder().system("FUGAZI SYSTEM").code("FUGAZI 2").build(),
                    Coding.builder().system("NACHO SYSTEM").code("NACHO").build()))
            .build();
    assertThatExceptionOfType(ResourceExceptions.BadRequestPayload.class)
        .isThrownBy(
            () ->
                ExtensionsHandlers.extractCodeableConceptValueForSystem(
                    cc, "foo", "FUGAZI SYSTEM"));
  }
}
