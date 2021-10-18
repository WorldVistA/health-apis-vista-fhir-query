package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
public class RequestPayloadModifier<ResourceT extends Resource> {

  private static final Pattern SITE_IN_PATH_PATTERN = Pattern.compile(".*/(hcs|site)/([-\\w]+)/.*");

  @NonNull private final HttpServletRequest request;

  @NonNull private final Consumer<Meta> addMeta;

  @Getter(lazy = true)
  private final Optional<String> site = determineSiteFromRequest();

  @NonNull ResourceT payload;

  @SuppressWarnings("unused")
  public static <T extends Resource> RequestPayloadModifierBuilder<T> forPayload(T payload) {
    return RequestPayloadModifier.<T>builder().payload(payload);
  }

  /**
   * Mutate the payload. This will attempt to determine the source site from the request URL and add
   * (or update) the meta.source field.
   */
  public void applyModifications() {
    if (!isPostOrPut()) {
      return;
    }
    updateMeta(payload);
    /*
     * Home for future expansion should we need to update other aspects of resources. We can add
     * pluggable functions and evaluate them here.
     */
  }

  private Optional<String> determineSiteFromRequest() {
    assert request != null;
    String uri = request.getRequestURI();
    if (isBlank(uri)) {
      return Optional.empty();
    }
    Matcher matcher = SITE_IN_PATH_PATTERN.matcher(uri);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    return Optional.ofNullable(matcher.group(2));
  }

  boolean isPostOrPut() {
    return ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod()));
  }

  private void updateMeta(ResourceT payload) {
    if (site().isEmpty()) {
      return;
    }
    if (payload.meta() == null) {
      addMeta.accept(Meta.builder().build());
    }
    payload.meta().source(site().get());
  }
}
