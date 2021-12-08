package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.referenceIdFromUri;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.IsSiteCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class ReferenceExtensionHandler extends AbstractSingleFieldExtensionHandler {
  @Getter private final String referenceFile;

  @Getter private final Function<String, IsSiteCoordinates> toCoordinates;

  @Builder
  ReferenceExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String fieldNumber,
      int index,
      @NonNull String referenceFile,
      @NonNull Function<String, IsSiteCoordinates> toCoordinates) {
    super(definingUrl, required, filemanFactory, fieldNumber, index);
    this.referenceFile = referenceFile;
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
    var referenceId =
        referenceIdFromUri(extension.valueReference())
            .orElseThrow(
                () ->
                    BadExtension.because(
                        extension.url(),
                        "Cannot determine reference id from .valueReference.reference"));
    var siteCoordinates = toCoordinates().apply(referenceId);
    if (siteCoordinates == null) {
      throw BadExtension.because(
          extension.url(), ".valueReference.reference could not be resolved to an id");
    }
    return List.of(
        filemanFactory()
            .forRequiredPointerWithGraveMarker(fieldNumber(), index(), siteCoordinates.ien()));
  }
}
