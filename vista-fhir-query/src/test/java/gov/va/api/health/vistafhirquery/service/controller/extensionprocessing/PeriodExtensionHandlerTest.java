package gov.va.api.health.vistafhirquery.service.controller.extensionprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.datatypes.Period;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.vistafhirquery.service.controller.ResourceExceptions.BadRequestPayload.BadExtension;
import gov.va.api.health.vistafhirquery.service.controller.WriteableFilemanValueFactory;
import gov.va.api.health.vistafhirquery.service.controller.extensionprocessing.ExtensionHandler.Required;
import gov.va.api.lighthouse.charon.models.lhslighthouserpcgateway.LhsLighthouseRpcGatewayCoverageWrite.WriteableFilemanValue;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

public class PeriodExtensionHandlerTest {
  static Stream<Arguments> badExtensionPeriod() {
    return Stream.of(arguments(Period.builder().build()));
  }

  static Stream<Arguments> handlePeriod() {
    return Stream.of(
        arguments(
            Period.builder().start("2010-01-20T10:00:01.000Z").end("2015-02-08").build(),
            List.of(
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(1)
                    .field(".start")
                    .value("01-20-2010")
                    .build(),
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(1)
                    .field(".end")
                    .value("02-08-2015")
                    .build())),
        arguments(
            Period.builder().start("2010-01-20T10:00:01.000Z").build(),
            List.of(
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(1)
                    .field(".start")
                    .value("01-20-2010")
                    .build())),
        arguments(
            Period.builder().end("2015-02-08").build(),
            List.of(
                WriteableFilemanValue.builder()
                    .file("888")
                    .index(1)
                    .field(".end")
                    .value("02-08-2015")
                    .build())));
  }

  private PeriodExtensionHandler _handler() {
    return PeriodExtensionHandler.forDefiningUrl("http://fugazi.com/period")
        .required(Required.REQUIRED)
        .filemanFactory(WriteableFilemanValueFactory.forFile("888"))
        .periodStartFieldNumber(".start")
        .periodEndFieldNumber(".end")
        .build();
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void badExtensionPeriod(Period period) {
    var sample = extensionWithPeriod(period);
    assertThatExceptionOfType(BadExtension.class).isThrownBy(() -> _handler().handle(sample));
  }

  private Extension extensionWithPeriod(Period period) {
    return Extension.builder().valuePeriod(period).build();
  }

  @ParameterizedTest
  @MethodSource
  void handlePeriod(Period period, List<WriteableFilemanValue> expected) {
    var sample = extensionWithPeriod(period);
    assertThat(_handler().handle(sample)).containsExactlyElementsOf(expected);
  }
}
