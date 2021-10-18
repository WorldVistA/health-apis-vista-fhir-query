package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class BooleanExtensionHandler extends AbstractExtensionHandler {

  @Getter @NonNull private final Map<Boolean, String> booleanStringMapping;

  @Builder
  public BooleanExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String fieldNumber,
      @NonNull Map<Boolean, String> booleanStringMapping) {
    super(definingUrl, required, fieldNumber, filemanFactory);
    this.booleanStringMapping = booleanStringMapping;
  }

  public static BooleanExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return BooleanExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(Extension extension) {
    var value = extension.valueBoolean();
    if (isBlank(value)) {
      throw BadExtension.because(definingUrl(), "extension.valueBoolean is null");
    }
    var filemanValue =
        filemanFactory().forString(fieldNumber(), 1, booleanStringMapping.get(value));
    return filemanValue == null ? List.of() : List.of(filemanValue);
  }
}
