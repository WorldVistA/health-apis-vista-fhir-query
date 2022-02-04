package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static java.util.stream.Collectors.toMap;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.resources.DomainResource;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.Map;
import java.util.function.Function;
import lombok.NonNull;

public class ContainedResourceReader {
  Map<String, Resource> containedResources;

  /** Constructor. */
  public ContainedResourceReader(DomainResource resource) {
    this.containedResources =
        resource == null
            ? Map.of()
            : Safe.stream(resource.contained()).collect(toMap(Resource::id, Function.identity()));
  }

  /** Get resource if contained resources have the specified reference. */
  public <T> T find(@NonNull Class<T> type, @NonNull String id) {
    Resource containedResource = containedResources.get(id);
    if (isBlank(containedResource) || !type.isInstance(containedResource)) {
      throw RequestPayloadExceptions.MissingContainedResource.builder()
          .resource(type.getSimpleName())
          .id(id)
          .build();
    }
    return type.cast(containedResource);
  }
}
