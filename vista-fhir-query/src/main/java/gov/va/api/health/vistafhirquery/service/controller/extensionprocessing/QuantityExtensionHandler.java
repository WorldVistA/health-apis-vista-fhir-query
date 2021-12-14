package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class QuantityExtensionHandler extends AbstractExtensionHandler {
  @Getter private final String valueFieldNumber;

  @Getter private final String unitFieldNumber;

  @Builder
  QuantityExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String valueFieldNumber,
      String unitFieldNumber,
      int index) {
    super(definingUrl, required, filemanFactory, index);
    this.valueFieldNumber = valueFieldNumber;
    this.unitFieldNumber = unitFieldNumber;
  }

  public static QuantityExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return QuantityExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    if (isBlank(extension.valueQuantity())) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueQuantity")
          .build();
    }
    var quantity = extension.valueQuantity();
    if (isBlank(quantity.value())) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueQuantity.value")
          .build();
    }
    List<WriteableFilemanValue> filemanValues = new ArrayList<>(2);
    filemanValues.add(
        filemanFactory()
            .forString(valueFieldNumber(), index(), quantity.value().toPlainString())
            .get());
    if (!isBlank(unitFieldNumber())) {
      filemanFactory()
          .forString(unitFieldNumber(), index(), quantity.unit())
          .ifPresentOrElse(
              filemanValues::add,
              () -> {
                throw ExtensionMissingRequiredField.builder()
                    .jsonPath(jsonPath)
                    .definingUrl(definingUrl())
                    .requiredFieldJsonPath(".valueQuantity.unit")
                    .build();
              });
    }
    return filemanValues;
  }
}
