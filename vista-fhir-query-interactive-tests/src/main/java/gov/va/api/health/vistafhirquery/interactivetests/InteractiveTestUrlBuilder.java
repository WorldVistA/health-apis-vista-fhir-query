package gov.va.api.health.vistafhirquery.interactivetests;

import java.net.URL;
import lombok.Builder;
import lombok.SneakyThrows;

@Builder
public class InteractiveTestUrlBuilder implements TestUrlBuilder {

  String baseUrl;
  String site;
  String resourceName;
  // Method method;
  // Map<String, String> parameters;

  @Override
  @SneakyThrows
  public URL url() {
    var url = String.format("%s/%s/r4/%s", baseUrl, site, resourceName);
    // TODO: Parse parameters and add to url
    return new URL(url);
  }
}
