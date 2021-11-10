package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.fhir.api.FhirDateTime.parseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
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

  @Getter private final String periodEndFieldNumber;

  @Builder
  PeriodExtensionHandler(
      @NonNull WriteableFilemanValueFactory filemanFactory,
      @NonNull String definingUrl,
      @NonNull ExtensionHandler.Required required,
      @NonNull String periodStartFieldNumber,
      @NonNull String periodEndFieldNumber,
      int index,
      @NonNull DateTimeFormatter dateTimeFormatter) {
    super(definingUrl, required, filemanFactory, index);
    this.dateTimeFormatter = dateTimeFormatter;
    this.periodStartFieldNumber = periodStartFieldNumber;
    this.periodEndFieldNumber = periodEndFieldNumber;
  }

  public static PeriodExtensionHandlerBuilder forDefiningUrl(String definingUrl) {
    return PeriodExtensionHandler.builder().definingUrl(definingUrl);
  }

  private String dateRange(String startDate, String endDate) {
    if (isBlank(startDate)) {
      throw BadExtension.because(definingUrl(), "Range does not have a start date.");
    }
    var range = formatDateString(startDate);
    if (!isBlank(endDate)) {
      range = range + "-" + formatDateString(endDate);
    }
    return range;
  }

  private String formatDateString(String dateString) {
    if (isBlank(dateString)) {
      return null;
    }
    return dateTimeFormatter().format(parseDateTime(dateString));
  }

  @Override
  public List<WriteableFilemanValue> handle(Extension extension) {
    if (isBlank(extension.valuePeriod())) {
      throw BadExtension.because(extension.url(), ".valuePeriod is null");
    }
    var period = extension.valuePeriod();
    if (allBlank(period.start(), period.end())) {
      throw BadExtension.because(extension.url(), ".valuePeriod does not have a start or end date");
    }
    if (Objects.equals(periodStartFieldNumber(), periodEndFieldNumber())) {
      var range = dateRange(period.start(), period.end());
      return List.of(filemanFactory().forRequiredString(periodStartFieldNumber(), index(), range));
    }
    return Stream.of(
            filemanFactory()
                .forOptionalString(
                    periodStartFieldNumber(), index(), formatDateString(period.start()))
                .orElse(null),
            filemanFactory()
                .forOptionalString(periodEndFieldNumber(), index(), formatDateString(period.end()))
                .orElse(null))
        .filter(Objects::nonNull)
        .toList();
  }
}
