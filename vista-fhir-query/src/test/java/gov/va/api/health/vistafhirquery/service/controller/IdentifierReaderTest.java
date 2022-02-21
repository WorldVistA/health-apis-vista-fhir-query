package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.vistafhirquery.service.controller.IdentifierReader.ReadableIdentifierDefinition;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.RequiredIdentifierIsMissing;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnknownIdentifierSystem;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import org.junit.jupiter.api.Test;

public class IdentifierReaderTest {
  List<ReadableIdentifierDefinition> _definitions() {
    return List.of(
        ReadableIdentifierDefinition.builder()
            .field("#1.1")
            .system("1001")
            .isRequired(true)
            .build(),
        ReadableIdentifierDefinition.builder()
            .field("#2.1")
            .system("2001")
            .isRequired(false)
            .build());
  }

  @Test
  void constructorThrowsUnexpectedNumberOfValues() {
    var identifiers =
        List.of(
            Identifier.builder().system("1001").value("100").build(),
            Identifier.builder().system("1001").value("100").build());
    assertThatExceptionOfType(RequestPayloadExceptions.UnexpectedNumberOfIdentifiers.class)
        .isThrownBy(
            () ->
                IdentifierReader.builder()
                    .readableIdentifierDefinitions(_definitions())
                    .filemanFactory(factory())
                    .indexRegistry(registry())
                    .build()
                    .process(identifiers));
  }

  WriteableFilemanValueFactory factory() {
    return WriteableFilemanValueFactory.forFile("1.01");
  }

  @Test
  void identifierWithUnknownSystemThrows() {
    var identifiers = List.of(Identifier.builder().system("NOPE").value("200").build());
    var reader =
        IdentifierReader.builder()
            .readableIdentifierDefinitions(_definitions())
            .filemanFactory(factory())
            .indexRegistry(registry())
            .build();
    assertThatExceptionOfType(UnknownIdentifierSystem.class)
        .isThrownBy(() -> reader.process(identifiers));
  }

  @Test
  void nullCheck() {
    var reader =
        IdentifierReader.builder()
            .readableIdentifierDefinitions(List.of())
            .filemanFactory(factory())
            .indexRegistry(registry())
            .build();
    assertThat(reader.process(null)).isEmpty();
  }

  @Test
  void processThrowsMissingRequiredField() {
    var identifiers = List.of(Identifier.builder().system("2001").value("200").build());
    var reader =
        IdentifierReader.builder()
            .readableIdentifierDefinitions(_definitions())
            .filemanFactory(factory())
            .indexRegistry(registry())
            .build();
    assertThatExceptionOfType(RequiredIdentifierIsMissing.class)
        .isThrownBy(() -> reader.process(identifiers));
  }

  @Test
  void processValidList() {
    var identifiers =
        List.of(
            Identifier.builder().system("1001").value("100").build(),
            Identifier.builder().system("2001").value("200").build());
    var reader =
        IdentifierReader.builder()
            .readableIdentifierDefinitions(_definitions())
            .filemanFactory(factory())
            .indexRegistry(registry())
            .build();
    assertThat(reader.process(identifiers))
        .containsExactlyInAnyOrder(
            WriteableFilemanValue.builder()
                .file("1.01")
                .index(1)
                .field("#1.1")
                .value("100")
                .build(),
            WriteableFilemanValue.builder()
                .file("1.01")
                .index(1)
                .field("#2.1")
                .value("200")
                .build());
  }

  FilemanIndexRegistry registry() {
    return FilemanIndexRegistry.create();
  }
}
