package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.resources.DomainResource;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@AllArgsConstructor(staticName = "of")
public class ContainedResourceWriter<R extends DomainResource> {

  @Getter(AccessLevel.PRIVATE)
  private final List<ContainableResource<R, ?>> includeResources;

  public void addContainedResources(R parentResource) {
    includeResources().forEach(r -> r.accept(parentResource));
  }

  @Value
  @Builder
  public static class ContainableResource<
          ParentT extends DomainResource, ContainedT extends Resource>
      implements Consumer<ParentT> {

    BiConsumer<ParentT, String> applyReferenceId;

    ContainedT containedResource;

    @Override
    public void accept(ParentT parentResource) {
      if (containedResource() == null) {
        return;
      }
      var id = UUID.randomUUID().toString();
      containedResource().id(id);
      if (isBlank(parentResource.contained())) {
        parentResource.contained(new ArrayList<>());
      } else {
        // Immutable Safety (e.g. List.of())
        parentResource.contained(new ArrayList<>(parentResource.contained()));
      }
      parentResource.contained().add(containedResource());
      applyReferenceId().accept(parentResource, "#" + id);
    }
  }
}
