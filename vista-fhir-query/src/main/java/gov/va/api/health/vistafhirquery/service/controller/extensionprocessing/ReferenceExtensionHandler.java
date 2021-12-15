package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.referenceIdFromUri;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.IsSiteCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionHasInvalidReferenceId;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class ReferenceExtensionHandler extends AbstractSingleFieldExtensionHandler {
  @Getter private final String referenceType;

  @Getter private final Function<String, IsSiteCoordinates> toCoordinates;

  @Builder
  ReferenceExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String fieldNumber,
      int index,
      @NonNull String referenceType,
      @NonNull Function<String, IsSiteCoordinates> toCoordinates) {
    super(definingUrl, required, filemanFactory, fieldNumber, index);
    this.referenceType = referenceType;
    this.toCoordinates = toCoordinates;
  }

  public static ReferenceExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return ReferenceExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    if (isBlank(extension.valueReference())) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueReference")
          .build();
    }
    var referenceIdCoordinates =
        referenceIdFromUri(extension.valueReference())
            .map(toCoordinates())
            .orElseThrow(
                () ->
                    ExtensionHasInvalidReferenceId.builder()
                        .jsonPath(".valueReference.reference")
                        .definingUrl(definingUrl())
                        .referenceType(referenceType())
                        .build());
    return List.of(
        filemanFactory()
            .forPointerWithGraveMarker(fieldNumber(), index(), referenceIdCoordinates.ien())
            .get());
  }
}
