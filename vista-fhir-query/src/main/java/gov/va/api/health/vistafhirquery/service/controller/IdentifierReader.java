package gov.va.api.health.vistafhirquery.service.controller;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.RequiredIdentifierIsMissing;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedNumberOfIdentifiers;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class IdentifierReader {

  Map<String, Identifier> identifiersBySystem;

  WriteableFilemanValueFactory filemanFactory;

  FilemanIndexRegistry indexRegistry;

  @Builder
  IdentifierReader(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull FilemanIndexRegistry indexRegistry,
      @NonNull Collection<Identifier> identifiers) {
    this.filemanFactory = filemanFactory;
    this.indexRegistry = indexRegistry;
    Map<String, Identifier> identifiersBySystem = new HashMap<>(identifiers.size());
    identifiers.forEach(
        identifier -> {
          if (identifiersBySystem.putIfAbsent(identifier.system(), identifier) != null) {
            var countOfDuplicates =
                identifiers.stream().filter(i -> i.system().equals(identifier.system())).count();
            throw UnexpectedNumberOfIdentifiers.builder()
                .exactExpectedCount(1)
                .receivedCount((int) countOfDuplicates)
                .jsonPath(".contained[].identifiers[]")
                .system("fieldNumber")
                .identifyingFieldValue(identifier.system())
                .build();
          }
        });
    this.identifiersBySystem = identifiersBySystem;
  }

  /** IdentifierReader processes a given collection of the expected identifier records. */
  public List<WriteableFilemanValue> process(Collection<IdentifierRecord> records) {
    return Safe.stream(records)
        .map(
            record -> {
              if (!identifiersBySystem().containsKey(record.system())) {
                if (record.isRequired()) {
                  throw RequiredIdentifierIsMissing.builder()
                      .system(record.system())
                      .jsonPath(".contained[].identifiers[].value")
                      .build();
                }
                return null;
              }
              return filemanFactory()
                  .forString(
                      record.fieldNumber(),
                      indexRegistry().get(record.fieldNumber()),
                      identifiersBySystem().get(record.system()).value())
                  .orElse(null);
            })
        .filter(Objects::nonNull)
        .collect(toList());
  }
}
