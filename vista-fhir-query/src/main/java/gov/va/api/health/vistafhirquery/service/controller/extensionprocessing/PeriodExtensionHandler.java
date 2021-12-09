package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static gov.va.api.health.fhir.api.FhirDateTime.parseDateTime;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.allBlank;
import static gov.va.api.health.vistafhirquery.service.controller.R4Transformers.isBlank;

import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExpectedAtLeastOneOfExtensionFields;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForExtensionField;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

  private String dateRange(String jsonPath, Optional<String> startDate, Optional<String> endDate) {
    if (isBlank(startDate)) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valuePeriod.start")
          .build();
    }
    var range = startDate.get();
    if (endDate.isPresent()) {
      range = range + "-" + endDate.get();
    }
    return range;
  }

  private Optional<String> formatDateString(String jsonPath, String dateString) {
    if (isBlank(dateString)) {
      return Optional.empty();
    }
    try {
      var dateTime = parseDateTime(dateString);
      return Optional.of(dateTimeFormatter().format(dateTime));
    } catch (DateTimeException | IllegalArgumentException e) {
      throw UnexpectedValueForExtensionField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .dataType("http://hl7.org/fhir/R4/datatypes.html#dateTime")
          .valueReceived(dateString)
          .build();
    }
  }

  @Override
  public List<WriteableFilemanValue> handle(String jsonPath, Extension extension) {
    if (isBlank(extension.valuePeriod())) {
      throw ExtensionMissingRequiredField.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .requiredFieldJsonPath(".valuePeriod")
          .build();
    }
    var start = formatDateString(".valuePeriod.start", extension.valuePeriod().start());
    var end = formatDateString(".valuePeriod.end", extension.valuePeriod().end());
    if (allBlank(start, end)) {
      throw ExpectedAtLeastOneOfExtensionFields.builder()
          .jsonPath(jsonPath)
          .definingUrl(definingUrl())
          .expectedAtLeastOneOfFields(List.of(".valuePeriod.start", ".valuePeriod.end"))
          .build();
    }
    if (Objects.equals(periodStartFieldNumber(), periodEndFieldNumber())) {
      var range = dateRange(jsonPath, start, end);
      return List.of(filemanFactory().forRequiredString(periodStartFieldNumber(), index(), range));
    }
    return Stream.of(
            filemanFactory()
                .forOptionalString(periodStartFieldNumber(), index(), start.orElse(null)),
            filemanFactory().forOptionalString(periodEndFieldNumber(), index(), end.orElse(null)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }
}
