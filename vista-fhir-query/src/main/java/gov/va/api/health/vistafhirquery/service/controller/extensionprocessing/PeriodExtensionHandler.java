package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.fhir.api.FhirDateTime.parseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class PeriodExtensionHandler extends AbstractExtensionHandler {
  @Getter private final DateTimeFormatter dateTimeFormatter;

  @Getter private final String periodStartFieldNumber;

  @Getter private final String periodEndFieldFormatter;

  @Builder
  PeriodExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String periodStartFieldNumber,
      @NonNull String periodEndFieldNumber,
      ZoneId zoneId) {
    super(definingUrl, required, filemanFactory);
    this.dateTimeFormatter =
        DateTimeFormatter.ofPattern("MM-dd-yyy")
            .withZone(zoneId == null ? ZoneId.of("UTC") : zoneId);
    this.periodStartFieldNumber = periodStartFieldNumber;
    this.periodEndFieldFormatter = periodEndFieldNumber;
  }

  public static PeriodExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return PeriodExtensionHandler.builder().definingUrl(definingUrl);
  }

  @Override
  public List<WriteableFilemanValue> handle(Extension extension) {
    if (isBlank(extension.valuePeriod())) {
      throw ResourceExceptions.BadRequestPayload.BadExtension.because(
          extension.url(), ".valuePeriod is null");
    }
    var period = extension.valuePeriod();
    if (allBlank(period.start(), period.end())) {
      throw ResourceExceptions.BadRequestPayload.BadExtension.because(
          extension.url(), ".valuePeriod does not have a start or end date");
    }
    return Stream.of(
            vistaFormatedDateFor(period.start(), periodStartFieldNumber()),
            vistaFormatedDateFor(period.end(), periodEndFieldFormatter()))
        .filter(Objects::nonNull)
        .toList();
  }

  private WriteableFilemanValue vistaFormatedDateFor(String datetime, String fieldNumber) {
    if (isBlank(datetime)) {
      return null;
    }
    var vistaFormatedDate = dateTimeFormatter().format(parseDateTime(datetime));
    return filemanFactory().forString(fieldNumber, 1, vistaFormatedDate);
  }
}
