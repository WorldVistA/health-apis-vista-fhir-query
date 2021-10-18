package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import static gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required.REQUIRED;
import static gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ExtensionProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class R4ExtensionProcessor implements ExtensionProcessor {
  private final Map<String, ExtensionHandler> handlers;

  private final Set<ExtensionHandler> requiredHandlers;

  private final Set<ExtensionHandler> usedHandlers;

  /** Get an extension processor that will use the given handlers. */
  public static R4ExtensionProcessor of(@NonNull ExtensionHandler... handlers) {
    return of(Arrays.asList(handlers));
  }

  /** Get an extension processor that will use the given list of handlers. */
  public static R4ExtensionProcessor of(@NonNull List<ExtensionHandler> handlers) {
    Map<String, ExtensionHandler> handlerMap =
        handlers.stream().collect(toMap(ExtensionHandler::definingUrl, Function.identity()));
    Set<ExtensionHandler> requiredHandlers =
        handlerMap.values().stream().filter(e -> REQUIRED.equals(e.required())).collect(toSet());
    return new R4ExtensionProcessor(handlerMap, requiredHandlers, new HashSet<>());
  }

  private void allRequiredExtensionsPresentOrDie() {
    if (!requiredHandlers.isEmpty()) {
      throw BadExtension.because(
          "Missing required extensions: "
              + requiredHandlers.stream().map(ExtensionHandler::definingUrl).toList());
    }
  }

  private ExtensionHandler findMatchingHandler(Extension ex) {
    hasDefiningUrlOrDie(ex);
    var matchingHandler = handlers.get(ex.url());
    if (matchingHandler == null) {
      throw BadExtension.because("Unknown extension found: " + ex.url());
    }
    if (REQUIRED.equals(matchingHandler.required())) {
      requiredHandlers.remove(matchingHandler);
    }
    return matchingHandler;
  }

  private void hasDefiningUrlOrDie(Extension ex) {
    if (isBlank(ex.url())) {
      throw BadExtension.because("Extensions must contain a defining URL.");
    }
  }

  @Override
  public List<WriteableFilemanValue> process(List<Extension> extensions) {
    List<WriteableFilemanValue> values = new ArrayList<>();
    for (Extension ex : extensions) {
      var matchingHandler = findMatchingHandler(ex);
      uniqueExtensionOrDie(matchingHandler);
      values.addAll(matchingHandler.handle(ex));
    }
    allRequiredExtensionsPresentOrDie();
    return values;
  }

  private void uniqueExtensionOrDie(ExtensionHandler matchingHandler) {
    if (!usedHandlers.add(matchingHandler)) {
      throw BadExtension.because(matchingHandler.definingUrl(), "Extensions must be unique.");
    }
  }
}
