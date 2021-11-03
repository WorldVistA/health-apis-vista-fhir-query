package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.datatypes.Quantity;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
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
      @NonNull String unitFieldNumber,
      int index) {
    super(definingUrl, required, filemanFactory, index);
    this.valueFieldNumber = valueFieldNumber;
    this.unitFieldNumber = unitFieldNumber;
  }

  public static QuantityExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return QuantityExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(Extension extension) {
    if (isBlank(extension.valueQuantity())) {
      throw BadExtension.because(extension.url(), ".valueQuantity is null");
    }
    var quantity = extension.valueQuantity();
    validQuantityOrDie(extension.url(), quantity);
    return List.of(
        filemanFactory().forString(valueFieldNumber(), index(), quantity.value().toString()),
        filemanFactory().forString(unitFieldNumber(), index(), quantity.unit()));
  }

  private void validQuantityOrDie(String definingUrl, Quantity quantity) {
    if (isBlank(quantity.value())) {
      throw BadExtension.because(definingUrl, ".valueQuantity.value is null");
    }
    if (isBlank(quantity.unit())) {
      throw BadExtension.because(definingUrl, ".valueQuantity.unit is null");
    }
  }
}
