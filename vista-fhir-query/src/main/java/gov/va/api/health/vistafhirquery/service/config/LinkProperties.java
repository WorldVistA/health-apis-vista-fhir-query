package gov.va.api.health.vistafhirquery.service.config;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.resources.Resource;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Properties for defining working links in responses. */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("vista-fhir-query")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class LinkProperties {

  private String publicUrl;
  private String publicR4BasePath;

  /**
   * Mapping for resource name and URL without resource name, e.g. Patient =
   * https://api.va.gov/services/fhir/v0/r4.
   */
  private Map<String, String> customR4UrlAndPath;

  private int defaultPageSize;
  private int maxPageSize;

  public Map<String, String> getCustomR4UrlAndPath() {
    return (customR4UrlAndPath == null) ? Map.of() : customR4UrlAndPath;
  }

  @PostConstruct
  void logConfiguration() {
    log.info("default page size: {}, max page size: {}", getDefaultPageSize(), getMaxPageSize());
    log.info("default R4 links: {}/{}", getPublicUrl(), getPublicR4BasePath());
    getCustomR4UrlAndPath().forEach((r, u) -> log.info("{} links: {}", r, u));
  }

  public Links r4() {
    return new Links(publicUrl, publicR4BasePath, getCustomR4UrlAndPath());
  }

  /** Links. */
  @Accessors(fluent = true)
  public static class Links {

    private static final String SITE_PLACEHOLDER = "{site}";

    private final String baseUrl;
    private final Map<String, String> urlForResource;

    Links(String publicUrl, String publicBasePath, @NonNull Map<String, String> publicR4Link) {
      baseUrl = publicUrl + "/" + publicBasePath;
      urlForResource = publicR4Link;
    }

    public String baseUrl(@NonNull String site) {
      return baseUrl.replace(SITE_PLACEHOLDER, site);
    }

    public String baseUrl(@NonNull String resource, @NonNull String site) {
      return urlForResource.getOrDefault(resource, baseUrl(site)).replace(SITE_PLACEHOLDER, site);
    }

    public String readUrl(@NonNull String site, @NonNull String resource, @NonNull String id) {
      return resourceUrl(site, resource) + "/" + id;
    }

    /**
     * Create a read link URL for the resource. If .meta.source, a site specific URL will be used.
     */
    public String readUrl(@NonNull Resource resource) {
      String site = siteForResource(resource);
      return isBlank(site)
          ? readUrlWithoutSite(resource.getClass().getSimpleName(), resource.id())
          : readUrl(site, resource.getClass().getSimpleName(), resource.id());
    }

    public String readUrlWithoutSite(@NonNull String resource, @NonNull String id) {
      return resourceUrlWithoutSite(resource) + "/" + id;
    }

    /**
     * Get the resource URL replacing, {site} with the provided site. Configured URLs without the
     * site placeholder are allowed, in which case substitution is ignored.
     */
    public String resourceUrl(@NonNull String site, @NonNull String resource) {
      return baseUrl(resource, site) + "/" + resource;
    }

    /**
     * Get the resource URL with no site information. URL configuration is verified to not contain
     * the site placeholder.
     */
    public String resourceUrlWithoutSite(@NonNull String resource) {
      /*
       * Since we're asking for url without a site, we do not want to substitute the site
       * placeholder and will use the raw baseUrl property. If a placeholder remains, either from
       * the resource specific override or the base URL, we'll throw an exception.
       */
      String url = urlForResource.getOrDefault(resource, baseUrl);
      if (url.contains(SITE_PLACEHOLDER)) {
        throw new UrlOrPathConfigurationException(
            "URL contains site placeholder, but not site is available: " + url);
      }
      return url + "/" + resource;
    }

    String siteForResource(@NonNull Resource resource) {
      return resource.meta() == null ? null : resource.meta().source();
    }
  }

  public static class UrlOrPathConfigurationException extends IllegalStateException {
    public UrlOrPathConfigurationException(String message) {
      super(message);
    }
  }
}
