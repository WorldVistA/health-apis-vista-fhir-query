package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

public class StringExtensionHandler extends AbstractExtensionHandler {

  @Builder
  public StringExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull AbstractExtensionHandler.IsRequired required,
      @NonNull String fieldNumber) {
    super(definingUrl, required, fieldNumber, filemanFactory);
  }

  public static StringExtensionHandler.StringExtensionHandlerBuilder forDefiningUrl(
      String definingUrl) {
    return StringExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue> handle(
      Extension extension) {
    var value = extension.valueString();
    if (isBlank(value)) {
      throw BadExtension.because(extension.url(), ".valueString is null");
    }
    return List.of(filemanFactory().forString(fieldNumber(), 1, value));
  }
}
