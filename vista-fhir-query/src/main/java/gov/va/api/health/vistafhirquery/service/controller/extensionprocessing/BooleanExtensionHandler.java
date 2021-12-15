package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForExtensionField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class BooleanExtensionHandler extends AbstractSingleFieldExtensionHandler {

  @Getter private final Map<Boolean, String> booleanStringMapping;

  /** All args constructor. */
  @Builder
  public BooleanExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String fieldNumber,
      int index,
      @NonNull Map<Boolean, String> booleanStringMapping) {
    super(definingUrl, required, filemanFactory, fieldNumber, index);
    this.booleanStringMapping = booleanStringMapping;
  }

  public static BooleanExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return BooleanExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    var value = extension.valueBoolean();
    if (isBlank(value)) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueBoolean")
          .build();
    }
    return List.of(
        filemanFactory()
            .forBoolean(fieldNumber(), index(), value, booleanStringMapping()::get)
            .orElseThrow(
                () ->
                    UnexpectedValueForExtensionField.builder()
                        .jsonPath(jsonPath)
                        .definingUrl(definingUrl())
                        .supportedValues(booleanStringMapping().keySet())
                        .valueReceived(value)
                        .build()));
  }
}
