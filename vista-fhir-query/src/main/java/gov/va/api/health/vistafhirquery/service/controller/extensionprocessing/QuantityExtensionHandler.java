package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
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
      throw RequestPayloadExceptions.ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueQuantity")
          .build();
    }
    var quantity = extension.valueQuantity();
    if (isBlank(quantity.value())) {
      throw BadExtension.because(definingUrl(), ".valueQuantity.value is null");
    }
    List<WriteableFilemanValue> filemanValues = new ArrayList<>(2);
    filemanValues.add(
        filemanFactory()
            .forRequiredString(valueFieldNumber(), index(), quantity.value().toPlainString()));
    if (!isBlank(unitFieldNumber())) {
      filemanFactory()
          .forOptionalString(unitFieldNumber(), index(), quantity.unit())
          .ifPresentOrElse(
              filemanValues::add,
              () -> {
                throw BadExtension.because(
                    definingUrl(), ".unit is required but was not specified.");
              });
    }
    return filemanValues;
  }
}
