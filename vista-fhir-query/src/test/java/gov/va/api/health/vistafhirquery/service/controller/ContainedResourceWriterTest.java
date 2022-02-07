package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Narrative;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.DomainResource;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;

class ContainedResourceWriterTest {
  @Test
  void writerAddsResourceToContainedWhenExists() {
    var dr = FugaziDomainResource.builder().id("fugazi").build();
    assertThat(dr.contained()).isNull();
    ContainedResourceWriter.of(
            List.of(
                ContainedResourceWriter.ContainableResource
                    .<FugaziDomainResource, FugaziResource>builder()
                    .applyReferenceId(
                        (f, id) -> f.fugaziReference(Reference.builder().id(id).build()))
                    .containedResource(FugaziResource.builder().build())
                    .build()))
        .addContainedResources(dr);
    assertThat(dr.contained()).hasSize(1);
    assertThat(dr.fugaziReference().id()).endsWith(dr.contained().get(0).id());
  }

  @Test
  void writerDoesNothingWhenContainedResourceIsNull() {
    var dr = FugaziDomainResource.builder().id("fugazi").build();
    assertThat(dr.contained()).isNull();
    ContainedResourceWriter.of(
            List.of(
                ContainedResourceWriter.ContainableResource
                    .<FugaziDomainResource, FugaziResource>builder()
                    .applyReferenceId(
                        (f, id) -> f.fugaziReference(Reference.builder().id(id).build()))
                    .containedResource(null)
                    .build()))
        .addContainedResources(dr);
    assertThat(dr.contained()).isNull();
  }

  @Test
  void writerIsSafeFromImmutableLists() {
    var dr =
        FugaziDomainResource.builder()
            .contained(List.of(FugaziResource.builder().language("ENG").build()))
            .id("fugazi")
            .build();
    assertThat(dr.contained()).hasSize(1);
    ContainedResourceWriter.of(
            List.of(
                ContainedResourceWriter.ContainableResource
                    .<FugaziDomainResource, FugaziResource>builder()
                    .applyReferenceId(
                        (f, id) -> f.fugaziReference(Reference.builder().id(id).build()))
                    .containedResource(FugaziResource.builder().language("DE").build())
                    .build()))
        .addContainedResources(dr);
    assertThat(dr.contained()).hasSize(2);
    assertThat("DE").isEqualTo(dr.contained().get(1).language());
    assertThat(dr.fugaziReference().id()).endsWith(dr.contained().get(1).id());
  }

  @Data
  @Builder
  static class FugaziResource implements Resource {
    @Builder.Default String resourceType = FugaziResource.class.getSimpleName();

    String id;

    String language;

    String implicitRules;

    Meta meta;
  }

  @Data
  @Builder
  static class FugaziDomainResource implements DomainResource {
    @Builder.Default String resourceType = FugaziDomainResource.class.getSimpleName();

    String id;

    String language;

    String implicitRules;

    Meta meta;

    List<Resource> contained;

    List<Extension> extension;

    List<Extension> modifierExtension;

    Narrative text;

    Reference fugaziReference;
  }
}
