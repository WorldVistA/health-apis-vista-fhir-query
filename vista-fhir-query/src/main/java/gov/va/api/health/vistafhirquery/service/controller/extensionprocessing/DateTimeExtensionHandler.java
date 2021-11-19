package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.fhir.api.FhirDateTime.parseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class DateTimeExtensionHandler extends AbstractExtensionHandler {
  @Getter private final DateTimeFormatter dateTimeFormatter;

  @Getter private final String dateTimeFieldNumber;

  @Builder
  DateTimeExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String dateTimeFieldNumber,
      int index,
      @NonNull DateTimeFormatter dateTimeFormatter) {
    super(definingUrl, required, filemanFactory, index);
    this.dateTimeFormatter = dateTimeFormatter;
    this.dateTimeFieldNumber = dateTimeFieldNumber;
  }

  public static DateTimeExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return DateTimeExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(Extension extension) {
    if (isBlank(extension.valueDateTime())) {
      throw BadExtension.because(extension.url(), ".valueDateTime is null");
    }
    return List.of(
        filemanFactory()
            .forRequiredString(
                dateTimeFieldNumber(),
                index(),
                dateTimeFormatter().format(parseDateTime(extension.valueDateTime()))));
  }
}
