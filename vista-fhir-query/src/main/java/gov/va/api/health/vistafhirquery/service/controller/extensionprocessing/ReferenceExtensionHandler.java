package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.referenceIdFromUri;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.IsSiteCoordinates;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.AbstractExtensionHandler.IsRequired;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class ReferenceExtensionHandler extends AbstractExtensionHandler {

  @Getter private final String referenceFile;

  @Getter private final Function<String, IsSiteCoordinates> toCoordinates;

  @Builder
  ReferenceExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull IsRequired required,
      @NonNull String fieldNumber,
      @NonNull String referenceFile,
      @NonNull Function<String, IsSiteCoordinates> toCoordinates) {
    super(definingUrl, required, fieldNumber, filemanFactory);
    this.referenceFile = referenceFile;
    this.toCoordinates = toCoordinates;
  }

  public static ReferenceExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return ReferenceExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public WriteableFilemanValue handle(Extension extension) {
    if (isBlank(extension.valueReference())) {
      throw BadExtension.because(extension.url(), ".valueReference is null");
    }
    var referenceId =
        referenceIdFromUri(extension.valueReference())
            .orElseThrow(
                () ->
                    BadExtension.because(
                        extension.url(),
                        "Cannot determine reference id from .valueReference.reference"));
    var siteCoordinates = toCoordinates().apply(referenceId);
    return filemanFactory().forPointer(referenceFile(), 1, siteCoordinates.ien());
  }
}
