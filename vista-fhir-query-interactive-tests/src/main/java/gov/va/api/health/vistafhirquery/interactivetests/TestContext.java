package gov.va.api.health.vistafhirquery.interactivetests;

import gov.va.api.health.r4.api.resources.Resource;

public interface TestContext {

  void create(Resource resource);

  String property(String key);
}
