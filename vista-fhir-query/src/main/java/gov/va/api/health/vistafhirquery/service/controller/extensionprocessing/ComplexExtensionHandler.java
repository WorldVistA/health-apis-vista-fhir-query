package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class ComplexExtensionHandler extends AbstractExtensionHandler {
  @Getter private final List<ExtensionHandler> childExtensions;

  @Builder
  ComplexExtensionHandler(
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull List<ExtensionHandler> childExtensions) {
    super(definingUrl, required, null);
    this.childExtensions = childExtensions;
  }

  public static ComplexExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return ComplexExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(Extension extension) {
    if (isBlank(extension.extension())) {
      throw ResourceExceptions.BadRequestPayload.BadExtension.because(
          extension.url(), ".extension is empty");
    }
    return R4ExtensionProcessor.of(childExtensions()).process(extension.extension());
  }
}
