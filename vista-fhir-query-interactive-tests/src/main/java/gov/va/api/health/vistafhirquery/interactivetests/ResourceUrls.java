package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.r4.api.elements.Reference;
import java.net.URL;
import java.util.Map;

public interface ResourceUrls {
  URL create();

  URL read(String id);

  default Reference reference(String id) {
    return Reference.builder().reference(read(id).toString()).build();
  }

  URL search(Map<String, String> parameters);

  URL update(String id);
}
