package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

public class DecimalExtensionHandler extends AbstractSingleFieldExtensionHandler {

  /** All args constructor. */
  @Builder
  public DecimalExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull Required required,
      @NonNull String fieldNumber,
      int index) {
    super(definingUrl, required, filemanFactory, fieldNumber, index);
  }

  public static DecimalExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return DecimalExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(@NonNull Extension extension) {
    var value = extension.valueDecimal();
    if (isBlank(value)) {
      throw ResourceExceptions.BadRequestPayload.BadExtension.because(
          definingUrl(), "extension.valueDecimal is null");
    }
    var filemanValue =
        filemanFactory().forRequiredString(fieldNumber(), index(), value.toPlainString());
    return filemanValue == null ? List.of() : List.of(filemanValue);
  }
}
