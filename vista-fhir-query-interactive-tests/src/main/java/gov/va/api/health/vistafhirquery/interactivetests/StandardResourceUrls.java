package gov.va.api.health.vistafhirquery.interactivetests;

import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.SneakyThrows;

@Builder
public class StandardResourceUrls implements ResourceUrls {
  String baseUrl;

  String site;

  String resourceName;

  @Override
  @SneakyThrows
  public URL create() {
    return new URL(String.format("%s/hcs/%s/r4/%s", baseUrl, site, resourceName));
  }

  @Override
  @SneakyThrows
  public URL read(String id) {
    return new URL(String.format("%s/hcs/%s/r4/%s/%s", baseUrl, site, resourceName, id));
  }

  @Override
  @SneakyThrows
  public URL search(Map<String, String> parameters) {
    return new URL(
        String.format("%s/hcs/%s/r4/%s?", baseUrl, site, resourceName)
            + parameters.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&")));
  }

  @Override
  @SneakyThrows
  public URL update(String id) {
    return new URL(String.format("%s/hcs/%s/r4/%s/%s", baseUrl, site, resourceName, id));
  }
}
