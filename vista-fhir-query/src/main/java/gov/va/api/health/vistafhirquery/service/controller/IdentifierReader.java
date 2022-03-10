package gov.va.api.health.vistafhirquery.service.controller;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.RequiredIdentifierIsMissing;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfIdentifiers;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnknownIdentifierSystem;
import gov.va.api.health.vistafhirquery.service.controller.definitions.MappableIdentifierDefinition;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class IdentifierReader {
  Map<String, ReadableIdentifierDefinition> handlersBySystem;

  WriteableFilemanValueFactory filemanFactory;

  FilemanIndexRegistry indexRegistry;

  @Builder
  IdentifierReader(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull FilemanIndexRegistry indexRegistry,
      @NonNull Collection<ReadableIdentifierDefinition> readableIdentifierDefinitions) {
    this.filemanFactory = filemanFactory;
    this.indexRegistry = indexRegistry;
    this.handlersBySystem =
        readableIdentifierDefinitions.stream()
            .collect(toMap(ReadableIdentifierDefinition::system, Function.identity()));
  }

  /** Build an IdentifierReader using MappableIdentifierDefinitions. */
  public static IdentifierReaderBuilder forDefinitions(
      Collection<MappableIdentifierDefinition> definitions) {
    return IdentifierReader.builder()
        .readableIdentifierDefinitions(
            definitions.stream()
                .map(
                    def ->
                        ReadableIdentifierDefinition.builder()
                            .field(def.vistaField())
                            .system(def.system())
                            .isRequired(def.isRequired())
                            .build())
                .toList());
  }

  /** IdentifierReader processes a given collection of the expected identifier records. */
  public List<WriteableFilemanValue> process(Collection<Identifier> identifiers) {
    List<String> allSystems = new ArrayList<>(handlersBySystem().keySet());
    List<String> requiredSystems =
        new ArrayList<>(
            handlersBySystem().values().stream()
                .filter(ReadableIdentifierDefinition::isRequired)
                .map(ReadableIdentifierDefinition::system)
                .toList());
    var results =
        Safe.stream(identifiers)
            .map(
                identifier -> {
                  var handler = handlersBySystem().get(identifier.system());
                  if (handler == null) {
                    throw UnknownIdentifierSystem.builder()
                        .jsonPath(".identifier[]")
                        .system(identifier.system())
                        .build();
                  }
                  if (!allSystems.remove(identifier.system())) {
                    var duplicateCount =
                        identifiers.stream()
                            .filter(e -> identifier.system().equals(e.system()))
                            .count();
                    throw UnexpectedNumberOfIdentifiers.builder()
                        .jsonPath(".identifier[]")
                        .exactExpectedCount(1)
                        .receivedCount((int) duplicateCount)
                        .system(identifier.system())
                        .build();
                  }
                  requiredSystems.remove(identifier.system());
                  return filemanFactory()
                      .forString(
                          handler.field(),
                          indexRegistry().get(filemanFactory().file()),
                          identifier.value())
                      .orElse(null);
                })
            .filter(Objects::nonNull)
            .collect(toList());
    if (!requiredSystems.isEmpty()) {
      throw RequiredIdentifierIsMissing.builder()
          .jsonPath(".identifier[]")
          .system(requiredSystems.get(0))
          .build();
    }
    return results;
  }

  @Value
  @Builder
  public static class ReadableIdentifierDefinition {
    @NonNull String system;

    @NonNull String field;

    boolean isRequired;
  }
}
