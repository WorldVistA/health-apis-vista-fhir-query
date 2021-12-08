package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.fhir.api.Safe;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.DuplicateExtension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingDefinitionUrl;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.MissingRequiredExtension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnknownExtension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class R4ExtensionProcessor implements ExtensionProcessor {
  private final String jsonPath;

  private final Map<String, ExtensionHandler> handlers;

  private final Set<ExtensionHandler> requiredHandlers;

  private final Set<ExtensionHandler> usedHandlers;

  /** Get an extension processor that will use the given handlers. */
  public static R4ExtensionProcessor of(String jsonPath, @NonNull ExtensionHandler... handlers) {
    return of(jsonPath, Arrays.asList(handlers));
  }

  /** Get an extension processor that will use the given list of handlers. */
  public static R4ExtensionProcessor of(String jsonPath, @NonNull List<ExtensionHandler> handlers) {
    Map<String, ExtensionHandler> handlerMap =
        handlers.stream().collect(toMap(ExtensionHandler::definingUrl, identity()));
    Set<ExtensionHandler> requiredHandlers =
        handlerMap.values().stream().filter(e -> REQUIRED.equals(e.required())).collect(toSet());
    return new R4ExtensionProcessor(jsonPath, handlerMap, requiredHandlers, new HashSet<>());
  }

  private void allRequiredExtensionsPresentOrDie() {
    if (!requiredHandlers.isEmpty()) {
      var firstHandler = requiredHandlers.stream().findFirst().get();
      throw MissingRequiredExtension.builder()
          .jsonPath(jsonPath)
          .definingUrl(firstHandler.definingUrl())
          .build();
    }
  }

  private ExtensionHandler findMatchingHandler(Extension ex) {
    hasDefiningUrlOrDie(ex);
    var matchingHandler = handlers.get(ex.url());
    if (matchingHandler == null) {
      throw UnknownExtension.builder().jsonPath(jsonPath).definingUrl(ex.url()).build();
    }
    if (REQUIRED.equals(matchingHandler.required())) {
      requiredHandlers.remove(matchingHandler);
    }
    return matchingHandler;
  }

  private void hasDefiningUrlOrDie(Extension ex) {
    if (isBlank(ex.url())) {
      throw MissingDefinitionUrl.builder().jsonPath(jsonPath).build();
    }
  }

  @Override
  public List<WriteableFilemanValue> process(List<Extension> extensions) {
    List<WriteableFilemanValue> values = new ArrayList<>();
    for (Extension ex : Safe.list(extensions)) {
      var matchingHandler = findMatchingHandler(ex);
      uniqueExtensionOrDie(matchingHandler);
      values.addAll(matchingHandler.handle(jsonPath, ex));
    }
    allRequiredExtensionsPresentOrDie();
    return values;
  }

  private void uniqueExtensionOrDie(ExtensionHandler matchingHandler) {
    if (!usedHandlers.add(matchingHandler)) {
      throw DuplicateExtension.builder()
          .jsonPath(jsonPath)
          .definingUrl(matchingHandler.definingUrl())
          .build();
    }
  }
}
