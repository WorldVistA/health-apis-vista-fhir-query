package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.resources.DomainResource;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor(staticName = "of")
public class ContainedResourceWriter<R extends DomainResource> {
  @Getter(AccessLevel.PRIVATE)
  private final List<ContainableResource<R, ?>> includeResources;

  /** Add all contained resources to parent using an incrementing identifier. */
  public void addContainedResources(R parentResource) {
    var incrementingIdentifier = new AtomicInteger(1);
    includeResources()
        .forEach(
            r -> r.accept(parentResource, () -> "" + incrementingIdentifier.getAndIncrement()));
  }

  @Value
  @Builder
  public static class ContainableResource<
          ParentT extends DomainResource, ContainedT extends Resource>
      implements BiConsumer<ParentT, Supplier<String>> {
    BiConsumer<ParentT, String> applyReferenceId;

    ContainedT containedResource;

    /** Add a resource to the parent resource using the given supplier to generate the id. */
    @Override
    public void accept(ParentT parentResource, Supplier<String> identifier) {
      if (containedResource() == null) {
        return;
      }
      var id = identifier.get();
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
