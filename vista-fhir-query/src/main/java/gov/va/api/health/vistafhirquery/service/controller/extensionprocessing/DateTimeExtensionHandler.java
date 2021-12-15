package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.tryFormatDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.tryParseDateTime;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForExtensionField;
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
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    if (isBlank(extension.valueDateTime())) {
      throw RequestPayloadExceptions.ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valueDateTime")
          .build();
    }
    String vistaDate =
        tryParseDateTime(extension.valueDateTime())
            .flatMap(dateTime -> tryFormatDateTime(dateTime, dateTimeFormatter))
            .orElseThrow(
                () ->
                    UnexpectedValueForExtensionField.builder()
                        .jsonPath(jsonPath)
                        .definingUrl(definingUrl())
                        .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
                        .valueReceived(extension.valueDateTime())
                        .build());
    return List.of(filemanFactory().forString(dateTimeFieldNumber(), index(), vistaDate).get());
  }
}
