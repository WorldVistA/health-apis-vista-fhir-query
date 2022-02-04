package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Narrative;
import gov.va.api.health.r4.api.resources.DomainResource;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;

class ContainedResourceReaderTest {
  @Test
  void containedResourceProcesses() {
    assertThat(
            new ContainedResourceReader(
                    Fugazi.builder().contained(List.of(Fugazi.builder().id("#1").build())).build())
                .containedResources)
        .isEqualTo(Map.of("#1", Fugazi.builder().id("#1").build()));
  }

  @Test
  void missingResourceThrowsMissingRequiredField() {
    assertThatExceptionOfType(RequestPayloadExceptions.MissingContainedResource.class)
        .isThrownBy(
            () -> new ContainedResourceReader(Fugazi.builder().build()).find(Fugazi.class, "#1"));
  }

  @Test
  void nullAndEmptyResourceHaveEmptyMap() {
    assertThat(new ContainedResourceReader(null).containedResources).isEqualTo(Map.of());
    assertThat(new ContainedResourceReader(Fugazi.builder().build()).containedResources)
        .isEqualTo(Map.of());
  }

  @Data
  @Builder
  private static class Fugazi implements DomainResource {
    List<Resource> contained;

    List<Extension> extension;

    List<Extension> modifierExtension;

    Narrative text;

    String implicitRules;

    String language;

    Meta meta;

    String id;
  }
}
