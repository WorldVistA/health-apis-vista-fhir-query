package gov.va.api.health.vistafhirquery.interactivetests;

import java.net.URL;
import java.util.Map;

public interface TestUrlBuilder {
  URL createUrl();

  URL readUrl(String id);

  URL searchUrl(Map<String, String> parameters);

  URL updateUrl(String id);
}
