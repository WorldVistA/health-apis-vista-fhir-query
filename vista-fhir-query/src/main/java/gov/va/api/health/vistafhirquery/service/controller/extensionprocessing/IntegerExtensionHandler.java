package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
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
      String jsonPath, @NonNull Extension extension) {
    var value = extension.valueInteger();
    if (isBlank(value)) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueInteger")
          .build();
    }
    return List.of(filemanFactory().forInteger(fieldNumber(), index(), value).get());
  }
}
