package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class FilemanIndexRegistryTest {
  @Test
  void getAndIncrementIncrements() {
    var registry = FilemanIndexRegistry.create();
    assertThat(registry.indexes()).isEmpty();
    for (int i = 1; i < 5; i++) {
      assertThat(registry.getAndIncrement("SHANKTOPUS")).isEqualTo(i);
    }
  }

  @Test
  void getDoesNotIncrement() {
    var registry = FilemanIndexRegistry.create();
    for (int i = 0; i < 5; i++) {
      assertThat(registry.get("SHANKTOPUS")).isEqualTo(1);
    }
  }

  @Test
  void getUnknownFileAddsNewIndex() {
    var registry = FilemanIndexRegistry.create();
    assertThat(registry.indexes()).isEmpty();
    for (int i = 1; i < 5; i++) {
      registry.get("SHANKTOPUS" + i);
      assertThat(registry.indexes().size()).isEqualTo(i);
    }
  }
}
