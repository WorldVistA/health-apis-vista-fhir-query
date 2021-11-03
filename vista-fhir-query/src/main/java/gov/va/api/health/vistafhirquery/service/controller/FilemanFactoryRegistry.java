package gov.va.api.health.vistafhirquery.service.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(staticName = "create")
public class FilemanFactoryRegistry {
  @Getter private Map<String, WriteableFilemanValueFactory> factories = new HashMap<>();

  /** Get a fileman factory for a given file or create a new one. */
  public WriteableFilemanValueFactory get(@NonNull String fileNumber) {
    var maybeFactory = factories().get(fileNumber);
    if (maybeFactory == null) {
      return newFactory(fileNumber);
    }
    return maybeFactory;
  }

  private WriteableFilemanValueFactory newFactory(@NonNull String fileNumber) {
    var factory = WriteableFilemanValueFactory.forFile(fileNumber);
    factories().put(fileNumber, factory);
    return factories().get(fileNumber);
  }
}
