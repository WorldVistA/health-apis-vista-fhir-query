package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.fhir.api.IsResource;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.Map;

public interface TestContext {
  void create(Resource resource);

  String property(String key);

  void search(Resource resource, Map<String, String> map);

  <T extends IsResource> ResourceUrls urlsFor(Class<T> resourceType);

  default <T extends IsResource> ResourceUrls urlsFor(T resource) {
    return urlsFor(resource.getClass());
  }
}
