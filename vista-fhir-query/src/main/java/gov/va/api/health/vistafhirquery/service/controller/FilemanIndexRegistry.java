package gov.va.api.health.vistafhirquery.service.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(staticName = "create")
public class FilemanIndexRegistry {
  @Getter private Map<String, AtomicInteger> indexes = new HashMap<>();

  /** Get the index for a file without incrementing it. */
  public Integer get(@NonNull String fileNumber) {
    return indexFor(fileNumber).get();
  }

  /** Return the current index for the file and increment its value. */
  public Integer getAndIncrement(@NonNull String fileNumber) {
    return indexFor(fileNumber).getAndIncrement();
  }

  private AtomicInteger indexFor(@NonNull String fileNumber) {
    var index = indexes().get(fileNumber);
    if (index == null) {
      return newIndex(fileNumber);
    }
    return index;
  }

  private AtomicInteger newIndex(@NonNull String fileNumber) {
    var newIndex = new AtomicInteger(1);
    indexes().put(fileNumber, newIndex);
    return indexes().get(fileNumber);
  }
}
