package gov.va.api.health.vistafhirquery.interactivetests;

import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.SneakyThrows;

@Builder
public class InteractiveTestUrlBuilder implements TestUrlBuilder {
  String baseUrl;

  String site;

  String resourceName;

  @Override
  @SneakyThrows
  public URL createUrl() {
    return new URL(String.format("%s/%s/r4/%s", baseUrl, site, resourceName));
  }

  @Override
  @SneakyThrows
  public URL readUrl(String id) {
    return new URL(String.format("%s/%s/r4/%s/%s", baseUrl, site, resourceName, id));
  }

  @Override
  @SneakyThrows
  public URL searchUrl(Map<String, String> parameters) {
    return new URL(
        String.format("%s/%s/r4/%s?", baseUrl, site, resourceName)
            + parameters.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&")));
  }

  @Override
  @SneakyThrows
  public URL updateUrl(String id) {
    return new URL(String.format("%s/%s/r4/%s/%s", baseUrl, site, resourceName, id));
  }
}
