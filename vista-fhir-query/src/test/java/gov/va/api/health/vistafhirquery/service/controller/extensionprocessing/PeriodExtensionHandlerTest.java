package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.ExtensionMissingRequiredField;
import gov.va.api.health.vistafhirquery.service.controller.RequestPayloadExceptions.UnexpectedValueForExtensionField;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

public class PeriodExtensionHandlerTest {
  static Stream<Arguments> badExtensionPeriod() {
    return Stream.of(
        arguments(ExtensionMissingRequiredField.class, null),
        arguments(BadExtension.class, Period.builder().build()));
  }

  static Stream<Arguments> handlePeriod() {
    return Stream.of(
        arguments(
            Period.builder().start("2010-01-20T10:00:01.000Z").end("2015-02-08").build(),
            2,
            List.of(
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(2)
                    .field(".start")
                    .value("01-20-2010")
                    .build(),
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(2)
                    .field(".end")
                    .value("02-08-2015")
                    .build())),
        arguments(
            Period.builder().start("2010-01-20T10:00:01.000Z").build(),
            1,
            List.of(
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(1)
                    .field(".start")
                    .value("01-20-2010")
                    .build())),
        arguments(
            Period.builder().end("2015-02-08").build(),
            1,
            List.of(
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(1)
                    .field(".end")
                    .value("02-08-2015")
                    .build())));
  }

  private PeriodExtensionHandler _handler(int index) {
    return PeriodExtensionHandler.forDefiningUrl("http://fugazi.com/period")
        .required(Required.REQUIRED)
        .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
        .dateTimeFormatter(DateTimeFormatter.ofPattern("MM-dd-yyy").withZone(ZoneId.of("UTC")))
        .periodStartFieldNumber(".start")
        .periodEndFieldNumber(".end")
        .index(index)
        .build();
  }

  @ParameterizedTest
  @MethodSource
  void badExtensionPeriod(Class<Exception> expected, Period period) {
    var sample = extensionWithPeriod(period);
    assertThatExceptionOfType(expected).isThrownBy(() -> _handler(1).handle(".fugazi", sample));
  }

  private Extension extensionWithPeriod(Period period) {
    return Extension.builder().valuePeriod(period).build();
  }

  @ParameterizedTest
  @MethodSource
  void handlePeriod(Period period, int index, List<WriteableFilemanValue> expected) {
    var sample = extensionWithPeriod(period);
    assertThat(_handler(index).handle(".fugazi", sample)).containsExactlyElementsOf(expected);
  }

  @Test
  void handleRange() {
    var handler =
        PeriodExtensionHandler.forDefiningUrl("http://fugazi.com/period")
            .required(Required.REQUIRED)
            .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
            .dateTimeFormatter(DateTimeFormatter.ofPattern("MMddyyy").withZone(ZoneId.of("UTC")))
            .periodStartFieldNumber(".range")
            .periodEndFieldNumber(".range")
            .index(1)
            .build();
    assertThat(
            handler.handle(
                ".fugazi", extensionWithPeriod(Period.builder().start("2020-01-20").build())))
        .containsOnly(
            WriteableFilemanValue.builder()
                .file("888")
                .index(1)
                .field(".range")
                .value("01202020")
                .build());
    assertThat(
            handler.handle(
                ".fugazi",
                extensionWithPeriod(
                    Period.builder().start("2020-01-20").end("2021-05-20T10:00:01.000Z").build())))
        .containsOnly(
            WriteableFilemanValue.builder()
                .file("888")
                .index(1)
                .field(".range")
                .value("01202020-05202021")
                .build());
  }

  @Test
  void handleRangeWithNoStartThrows() {
    var handler =
        PeriodExtensionHandler.forDefiningUrl("http://fugazi.com/period")
            .required(Required.REQUIRED)
            .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
            .dateTimeFormatter(DateTimeFormatter.ofPattern("MMddyyy").withZone(ZoneId.of("UTC")))
            .periodStartFieldNumber(".range")
            .periodEndFieldNumber(".range")
            .index(1)
            .build();
    assertThatExceptionOfType(BadExtension.class)
        .isThrownBy(
            () ->
                handler.handle(
                    ".fugazi",
                    extensionWithPeriod(Period.builder().end("2021-05-20T10:00:01.000Z").build())));
  }

  @ParameterizedTest
  @CsvSource(value = {"2015-02-08,Feb 8 2015", "Feb 8 2015,2015-02-08"})
  void misformattedDatesThrow(String start, String end) {
    var sample = extensionWithPeriod(Period.builder().start(start).end(end).build());
    assertThatExceptionOfType(UnexpectedValueForExtensionField.class)
        .isThrownBy(() -> _handler(1).handle(".period", sample));
  }
}
