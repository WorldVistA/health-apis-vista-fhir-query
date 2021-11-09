package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

public class IntegerExtensionHandler extends AbstractSingleFieldExtensionHandler {
  /** All args constructor. */
  @Builder
  public IntegerExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull Required required,
      @NonNull String fieldNumber,
      int index) {
    super(definingUrl, required, filemanFactory, fieldNumber, index);
  }

  public static IntegerExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return IntegerExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue> handle(
      @NonNull Extension extension) {
    var value = extension.valueInteger();
    if (isBlank(value)) {
      throw ResourceExceptions.BadRequestPayload.BadExtension.because(
          definingUrl(), "extension.valueInteger is null");
    }
    var filemanValue = filemanFactory().forInteger(fieldNumber(), index(), value);
    return filemanValue == null ? List.of() : List.of(filemanValue);
  }
}
