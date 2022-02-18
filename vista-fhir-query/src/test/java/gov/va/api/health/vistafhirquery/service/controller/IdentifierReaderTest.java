package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import org.junit.jupiter.api.Test;

public class IdentifierReaderTest {
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
                    .identifiers(identifiers)
                    .filemanFactory(factory())
                    .indexRegistry(registry())
                    .build());
  }

  WriteableFilemanValueFactory factory() {
    return WriteableFilemanValueFactory.forFile("1.01");
  }

  List<IdentifierRecord> identifierRecords() {
    return List.of(
        IdentifierRecord.builder().fieldNumber("#1.1").system("1001").isRequired(true).build(),
        IdentifierRecord.builder().fieldNumber("#2.1").system("2001").isRequired(false).build());
  }

  @Test
  void nullCheck() {
    var identifiers = List.of(Identifier.builder().system("2001").value("200").build());
    var reader =
        IdentifierReader.builder()
            .identifiers(identifiers)
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
            .identifiers(identifiers)
            .filemanFactory(factory())
            .indexRegistry(registry())
            .build();
    assertThatExceptionOfType(RequestPayloadExceptions.RequiredIdentifierIsMissing.class)
        .isThrownBy(() -> reader.process(identifierRecords()));
  }

  @Test
  void processValidList() {
    var identifiers =
        List.of(
            Identifier.builder().system("1001").value("100").build(),
            Identifier.builder().system("2001").value("200").build());
    var reader =
        IdentifierReader.builder()
            .identifiers(identifiers)
            .filemanFactory(factory())
            .indexRegistry(registry())
            .build();
    assertThat(reader.process(identifierRecords()))
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
